package mash.ui

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.ashley.allOf
import ktx.collections.GdxArray
import ktx.collections.toGdxArray
import ktx.scene2d.*
import net.mgsx.gltf.scene3d.lights.SpotLightEx
import threedee.ecs.components.SceneComponent
import twodee.core.engine
import twodee.core.selectedItemListOf
import twodee.ecs.ashley.components.Player
import twodee.ui.LavaHud

object Share3dDebugData {
    val selectedNodes = mutableSetOf<UiNode>()
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
                    if(Share3dDebugData.selectedNodes.contains(uiNode))
                        Share3dDebugData.selectedNodes.remove(uiNode)
                    else
                        Share3dDebugData.selectedNodes.add(uiNode)
                }
                createNodeHierarchy(node, node.children.toGdxArray(), it)
            }
        }
    }

    /**
     * Lazy loading fixes EVERYTHING in every part of a system!
     */
    override val stage by lazy {
        Stage(hudViewPort, batch).apply {
            actors {
                table {
                    setFillParent(true)
                    top()
                    label("Hello World!")
                    tree {
                        label("root") {
                            createNodeHierarchy(null, getPlayerNodes(), it)
                        }
                    }
                }
            }
            inputMultiplexer.addProcessor(this)
        }
    }
}