package mash.ui

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.scene2d.actors
import ktx.scene2d.label
import twodee.ui.LavaHud

class ToolHud(batch: PolygonSpriteBatch) : LavaHud(batch) {
    override val stage = Stage(hudViewPort, batch).apply {
        actors {
            label("Hello World!")
        }
    }
}