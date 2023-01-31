package mash.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.MainGame
import eater.injection.InjectionContext.Companion.inject
import mash.factories.SceneLoader

class GameScreen(
    private val sceneLoader: SceneLoader,
//    private val stage: Stage,
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

    override fun render(delta: Float) {
        super.render(delta)
//        stage.act(delta)
//        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
//        stage.viewport.update(width, height)
    }


    override fun dispose() {
        super.dispose()
        sceneLoader.dispose()
//        stage.dispose()
    }
}
