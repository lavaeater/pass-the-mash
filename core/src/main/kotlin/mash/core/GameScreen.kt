package mash.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.vfx.VfxManager
import mash.factories.SceneLoader
import twodee.core.MainGame
import twodee.injection.InjectionContext.Companion.inject

class GameScreen(
    private val sceneLoader: SceneLoader,
    private val vfxManager: VfxManager,
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


    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        vfxManager.resize(width, height)
    }

    override fun dispose() {
        super.dispose()
        sceneLoader.dispose()
    }
}
