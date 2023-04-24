package mash.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.ashley.entity
import ktx.ashley.with
import twodee.core.MainGame
import twodee.injection.InjectionContext.Companion.inject
import mash.factories.SceneLoader
import threedee.ecs.components.CameraComponent
import threedee.ecs.components.KeyboardControlComponent
import twodee.core.engine

class GameScreen(
    private val sceneLoader: SceneLoader,
    game: MainGame,
    engine: Engine,
    viewport: ExtendViewport
) : Screen3d(game, engine, viewport) {
    private var needsInit = true
    override fun show() {
        super.show()
        if (needsInit) {
            needsInit = false
            createCameraEntity()

            sceneLoader.loadScene(inject(), inject())
        }
    }

    private fun createCameraEntity() {
        engine().entity {
            with<CameraComponent> {
                camera = viewport.camera
            }
            with<KeyboardControlComponent>()
        }
    }


    override fun dispose() {
        super.dispose()
        sceneLoader.dispose()
    }
}
