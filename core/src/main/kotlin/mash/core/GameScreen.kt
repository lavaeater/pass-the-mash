package mash.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.MainGame
import eater.injection.InjectionContext.Companion.inject
import mash.factories.SceneLoader

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
            sceneLoader.loadScene(inject(), inject())
        }
    }


    override fun dispose() {
        super.dispose()
        sceneLoader.dispose()
    }
}
