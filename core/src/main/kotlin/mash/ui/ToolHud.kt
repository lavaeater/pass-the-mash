package mash.ui

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.ashley.allOf
import ktx.collections.GdxArray
import ktx.collections.toGdxArray
import ktx.math.vec3
import ktx.scene2d.*
import net.mgsx.gltf.scene3d.lights.SpotLightEx
import threedee.ecs.components.SceneComponent
import twodee.core.engine
import twodee.ecs.ashley.components.Player
import twodee.ui.LavaHud

sealed class UiNode(var selected: Boolean = false) {
    open var name: String = ""

    class ThreeDNode(val node: Node, val parent: Node?) : UiNode() {
        init {
            node.id = node.id.replace("mixamorig:", "")
        }

        override var name: String
            get() = node.id
            set(value) {
                node.id = value
            }
    }

    class SpotLightNode(val spotLightEx: SpotLightEx) : UiNode()
    object EmptyNode : UiNode() {
        override var name: String
            get() = "Empty"
            set(value) {}
    }
}

class ToolHud(batch: PolygonSpriteBatch, private val inputMultiplexer: InputMultiplexer) : LavaHud(batch) {
    private val playerFamily by lazy { allOf(SceneComponent::class, Player::class).get() }
    private val playerEntity by lazy {
        engine()
            .getEntitiesFor(playerFamily)
            .first()
    }

    private fun getPlayerNodes(): GdxArray<Node> {
        return SceneComponent.get(playerEntity).scene.modelInstance.nodes
    }


    private fun createNodeHierarchy(parent3dNode: Node?, nodes: GdxArray<Node>, parentNode: KNode<*>) {
        nodes.forEach { node ->
            val uiNode = UiNode.ThreeDNode(node, parent3dNode)
            parentNode.label(uiNode.name) {
                onClick {
                    Share3dDebugData.selectedNode = uiNode
                }
                createNodeHierarchy(node, node.children.toGdxArray(), it)
            }
        }
    }

    val translationVector = vec3()

    /**
     * Now you need to flexbox this UI into something usable.
     *
     * The tree must be expanded at all times.
     */
    override val stage by lazy {
        lateinit var nodeTree: KTreeWidget
        Stage(hudViewPort, batch).apply {
            isDebugAll = true
            actors {
                nodeTree = tree {
                    label("root") {
                        createNodeHierarchy(null, getPlayerNodes(), it)
                    }
                    expandAll()
                    isVisible = false
                    color = Color.BLUE
                    setPosition(0f, hudViewPort.worldHeight)
                }
                table {
                    // MAIN TABLE
                    setFillParent(true)
                    table {
                        // TOP ROW
                        table {
                            // TREE TABLE
                            label("TOP LEFT")

                        }
                            .inCell
                            .left()
                            .top()
                            .fill()
                            .expand()
                        table {
                            // CENTER TOP COLUMN
                            label("CENTER TOP")
                            row()
                        }
                            .inCell
                            .center()
                            .expand()
                        table {
                            // TOP RIGHT COLUMN
                            label("TOP RIGHT")
                            row()
                        }
                            .inCell
                            .right()
                            .expand()
                    }
                        .inCell
                        .top()
                        .height(hudViewPort.worldHeight * 0.1f)
                    row()
                    table {
                        // MIDDLE TABLE
                        verticalGroup {
                            // LEFT COLUMN
                            label("Left Column")
                        }
                            .inCell
                            .left()
                            .fillY()
                            .width(hudViewPort.worldWidth * 0.1f)
                        table {
                            // CENTER TABLE
                            label("CENTER TABLE")
                            row()
                        }
                            .inCell
                            .fill()
                            .expand()
                        verticalGroup {
                            // RIGHT COLUMN
                            label("Right Column")
                        }
                            .inCell
                            .right()
                            .fillY()
                            .width(hudViewPort.worldWidth * 0.1f)

                    }
                        .inCell
                        .fill()
                        .expand()
                    row()
                    table {
                        // BOTTOM ROW
                        button {
                            label("Toggle Nodes")
                            onClick {
                                nodeTree.isVisible = !nodeTree.isVisible
                            }
                        }
                            .inCell
                            .top()
                            .left()
                        verticalGroup {
                            checkBox("Draw Skeleton") {
                                isChecked = Share3dDebugData.drawSkeleton
                                onChange {
                                    Share3dDebugData.drawSkeleton = isChecked
                                }
                            }
                                .left()
                            checkBox("Draw Bullet World") {
                                isChecked = Share3dDebugData.drawBulletDebug
                                onChange {
                                    Share3dDebugData.drawBulletDebug = isChecked
                                }
                            }
                                .left()
                            checkBox("Draw Debug Node") {
                                isChecked = Share3dDebugData.drawDebugNode
                                onChange {
                                    Share3dDebugData.drawDebugNode = isChecked
                                }
                            }
                                .left()
                        }
                            .inCell
                            .top()
                            .center()
                        row()
                    }
                        .inCell
                        .left()
                        .height(hudViewPort.worldHeight * 0.25f)


//                    boundLabel({ Share3dDebugData.selectedNode.name })
//                    boundLabel({
//                        when(Share3dDebugData.selectedNode) {
//                            is UiNode.ThreeDNode -> (Share3dDebugData.selectedNode as UiNode.ThreeDNode).node.localTransform.getTranslation(translationVector).toString()
//                            is UiNode.SpotLightNode -> "spot light"
//                            is UiNode.EmptyNode -> "empty"
//                        }
//                    })
                }
            }
            inputMultiplexer.addProcessor(this)
        }
    }
}