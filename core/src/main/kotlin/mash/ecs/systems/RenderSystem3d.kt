package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import twodee.physics.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.graphics.use
import mash.injection.GameSettings
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import threedee.ecs.components.AddedToRenderableList
import threedee.ecs.components.SceneComponent
import threedee.ecs.components.VisibleComponent
import threedee.gfx.NestableFrameBuffer

class RenderSystem3d(
    private val sceneManager: SceneManager,
    private val spriteBatch: PolygonSpriteBatch,
    private val gameSettings: GameSettings,
    private val camera: Camera
) : IteratingSystem(
    allOf(
        SceneComponent::class,
        VisibleComponent::class
    )
        .exclude(AddedToRenderableList::class).get()
) {
    private val lowResBuffer = NestableFrameBuffer(Pixmap.Format.RGBA8888, gameSettings.frameBufferWidth.toInt(), gameSettings.gameHeight.toInt(), true, false)

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        renderScenes(deltaTime)
    }

    private val scenesToRender = mutableListOf<Scene>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val sceneComponent = SceneComponent.get(entity)
        if (!sceneComponent.added) {
            scenesToRender.add(sceneComponent.scene)
            sceneComponent.added = true // why not...
            entity.addComponent<AddedToRenderableList>()
        }
    }

    private val width get() = Gdx.graphics.width.toFloat()
    private val height get() = Gdx.graphics.height.toFloat()

    private fun renderScenes(deltaTime: Float) {
        camera.update()
        sceneManager.update(deltaTime)
        sceneManager.renderShadows()
        lowResBuffer.begin()
        ScreenUtils.clear(Color.SKY, true);
        sceneManager.renderColors()
        lowResBuffer.end()
        val texture = lowResBuffer.colorBufferTexture
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

        spriteBatch.use {
             it.draw(texture, 0f, 0f, width, height,0f, 0f, 1f, 1f)
        }
    }
}
