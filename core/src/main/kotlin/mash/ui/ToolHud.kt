package mash.ui

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import ktx.ashley.allOf
import ktx.collections.GdxArray
import ktx.collections.toGdxArray
import ktx.scene2d.*
import net.mgsx.gltf.scene3d.scene.Scene
import threedee.ecs.components.SceneComponent
import twodee.core.engine
import twodee.ecs.ashley.components.Player
import twodee.ui.LavaHud

class ToolHud(batch: PolygonSpriteBatch) : LavaHud(batch) {
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
            parentNode.label(node.id) {
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
                            it.expandAll()
                        }
                    }

                }
            }
        }
    }
}