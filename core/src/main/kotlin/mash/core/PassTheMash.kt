package mash.core

import com.badlogic.gdx.physics.bullet.Bullet
import mash.injection.Context
import eater.core.MainGame
import eater.injection.InjectionContext.Companion.inject
import ktx.async.KtxAsync
import mash.factories.CarSceneLoader

class PassTheMash : MainGame() {
    override fun goToGameSelect() {
        TODO("Not yet implemented")
    }

    override fun goToGameScreen() {
        TODO("Not yet implemented")
    }

    override fun goToGameOver() {
        TODO("Not yet implemented")
    }

    override fun gotoGameVictory() {
        TODO("Not yet implemented")
    }

    override fun create() {
        KtxAsync.initiate()
        Bullet.init()
        Context.initialize(this)

        addScreen(
            inject<GameScreen>()
        )
        setScreen<GameScreen>()
    }
}
