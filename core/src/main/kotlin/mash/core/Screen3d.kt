package mash.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.utils.viewport.ExtendViewport
import twodee.core.MainGame
import twodee.core.toColor
import ktx.app.KtxScreen
import ktx.app.clearScreen

open class Screen3d(
    protected val game: MainGame,
    protected val engine: Engine,
    protected val viewport: ExtendViewport
) : KtxScreen {
    protected val bgColor = "000000".toColor()
    override fun render(delta: Float) {
        clearScreen(bgColor.r, bgColor.g, bgColor.b)
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}
