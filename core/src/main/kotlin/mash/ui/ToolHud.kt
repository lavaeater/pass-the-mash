package mash.ui

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.ashley.allOf
import ktx.collections.GdxArray
import ktx.collections.toGdxArray
import ktx.math.vec3
import ktx.scene2d.*
import threedee.ecs.components.SceneComponent
import twodee.core.engine
import twodee.ecs.ashley.components.Player
import twodee.extensions.boundLabel
import twodee.extensions.boundTextField
import twodee.ui.LavaHud

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
    val rotation = Quaternion()

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
                        table {
                            boundLabel({ Share3dDebugData.selectedNode.name })
                                .inCell
                                .top()
                                .center()
                            row()
                            table {
                                verticalGroup {
                                    label("X")
                                    boundLabel({
                                        Share3dDebugData.nodeTranslation.x.toString()
                                    })
                                    horizontalGroup {
                                        button { label("X+") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(0.1f, 0f, 0f)
                                                    this.node.calculateTransforms(true)
                                                }
                                            }
                                        button { label("X-") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(-0.1f, 0f, 0f)
                                                    this.node.calculateTransforms(true)
                                                }
                                            }
                                    }
                                }
                                    .inCell
                                    .expand()
                                    .fill()
                                verticalGroup {
                                    label("Y")
                                    boundLabel({
                                        Share3dDebugData.nodeTranslation.y.toString()
                                    })
                                    horizontalGroup {
                                        button { label("Y+") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(0f, 0.1f, 0f)
                                                    this.node.calculateTransforms(true)
                                                }
                                            }
                                        button { label("Y-") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(0f, -0.1f, 0f)
                                                    this.node.calculateTransforms(true)
                                                }
                                            }
                                    }
                                }
                                    .inCell
                                    .expand()
                                    .fill()
                                verticalGroup {
                                    label("Z")
                                    boundLabel({
                                        Share3dDebugData.nodeTranslation.z.toString()
                                    })
                                    horizontalGroup {
                                        button { label("Z+") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(0f, 0f, 0.1f)
                                                    this.node.calculateTransforms(true)
                                                }
                                            }
                                        button { label("Z-") }
                                            .onClick {
                                                (Share3dDebugData.selectedNode as UiNode.ThreeDNode).apply {
                                                    this.node.translation.add(0f, 0f, -0.1f)
                                                    this.node.calculateTransforms(true)
                                                }

                                            }
                                    }
                                }
                                    .inCell
                                    .expand()
                                    .fill()
                            }
                                .inCell
                                .top()
                                .center()
                        }
                            .inCell
                            .left()
                            .width(hudViewPort.worldWidth * 0.25f)
                    }
                        .inCell
                        .left()
                        .height(hudViewPort.worldHeight * 0.25f)
                }
            }
            inputMultiplexer.addProcessor(this)
        }
    }
}