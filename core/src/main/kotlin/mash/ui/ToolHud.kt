package mash.ui

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.ashley.allOf
import ktx.collections.GdxArray
import ktx.collections.toGdxArray
import ktx.math.vec3
import ktx.scene2d.*
import net.mgsx.gltf.scene3d.lights.SpotLightEx
import threedee.ecs.components.SceneComponent
import twodee.core.engine
import twodee.core.selectedItemListOf
import twodee.ecs.ashley.components.Player
import twodee.extensions.boundLabel
import twodee.ui.LavaHud

object Share3dDebugData {
    /**
     * I do not actually need a LIST of nodes, I could probably do with just a single node.
     *
     * But we will do this - but then
     */
//    val selectedNodes = mutableSetOf<UiNode>()
    var selectedNode: UiNode = UiNode.EmptyNode

}

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
        Stage(hudViewPort, batch).apply {
            isDebugAll = true
            actors {
                table {
                    setFillParent(true)
                    table {
                        top()
                        label("Top Row")
                    }
                        .inCell
                        .height(hudViewPort.worldHeight * 0.1f)
                    row()
                    table {
                        verticalGroup {
                            label("Left Column")
                            label("Middle")
                            label("Right Column")
                        }
                            .inCell
                            .left()
                            .fillY()
                            .width(hudViewPort.worldWidth * 0.1f)
                        table {

                        }
                            .inCell
                            .fill()
                            .expand()
                        verticalGroup {
                            label("Left Column")
                            label("Middle")
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
                        label("Bottom Row")
                    }
                        .inCell
                        .height(hudViewPort.worldHeight * 0.1f)

//                    tree {
//                        label("root") {
//                            createNodeHierarchy(null, getPlayerNodes(), it)
//                        }
//                        expandAll()
//                    }
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