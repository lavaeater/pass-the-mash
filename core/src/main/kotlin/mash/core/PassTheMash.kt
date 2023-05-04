package mash.core

import com.badlogic.gdx.physics.bullet.Bullet
import mash.injection.Context
import twodee.core.MainGame
import twodee.injection.InjectionContext.Companion.inject
import ktx.async.KtxAsync

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
        Context.initialize(this)

        addScreen(
            inject<GameScreen>()
        )
        setScreen<GameScreen>()
    }
}
