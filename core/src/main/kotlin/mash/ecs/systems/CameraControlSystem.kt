package mash.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController

class CameraControlSystem(private val camera: PerspectiveCamera) : EntitySystem() {
    private val controller = CameraInputController(camera)
    init {
        Gdx.input.inputProcessor = controller
    }

    override fun update(deltaTime: Float) {
        controller.update()
    }
}