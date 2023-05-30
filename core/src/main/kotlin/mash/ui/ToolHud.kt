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
import twodee.ecs.ashley.components.Player
import twodee.ui.LavaHud

object Share3dDebugData {
    val selectedNodes = mutableSetOf<Node>()
}

sealed class UiNode(var selected: Boolean = false) {
    class ThreeDNode(val node: Node) : UiNode()
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



    private fun createNodeHierarchy(nodes: GdxArray<Node>, parentNode: KNode<*>) {
        for (node in nodes) {
            parentNode.label(node.id.replace("mixamorig:", "")) {
                onClick {
                    if(Share3dDebugData.selectedNodes.contains(node))
                        Share3dDebugData.selectedNodes.remove(node)
                    else
                        Share3dDebugData.selectedNodes.add(node)
                }
                createNodeHierarchy(node.children.toGdxArray(), it)
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
                            createNodeHierarchy(getPlayerNodes(), it)
                        }
                    }
                }
            }
            inputMultiplexer.addProcessor(this)
        }
    }
}