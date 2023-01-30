package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import depth.ecs.components.Camera3dFollowComponent
import depth.ecs.components.MotionStateComponent
import ktx.ashley.allOf
import ktx.math.plus
import ktx.math.times
import ktx.math.unaryMinus
import ktx.math.vec3

class UpdatePerspectiveCameraSystem(
    private val perspectiveCamera: PerspectiveCamera
) :
    IteratingSystem(
        allOf(
            MotionStateComponent::class,
            Camera3dFollowComponent::class
        ).get()
    ) {

    val target = vec3()
    private val tmpVector = vec3()
    private val rotatedOffset = vec3()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val motionState = MotionStateComponent.get(entity)

        val position = motionState.position
        val cc = Camera3dFollowComponent.get(entity)
        rotatedOffset.set(motionState.backwards).scl(cc.offsetXZ.y).add(0f, cc.offsetY, 0f)
        tmpVector.set(position).add(rotatedOffset)
        perspectiveCamera.position.lerp(tmpVector, 0.2f)
//
//        val offset = cc.offset
//        info { "${cc.offsetDirection}" }
//        tmpVector
//            .set(motionState.forward * cc.offsetDirection)
//            .scl(cc.distance)
//            .add(offset.x, offset.y, offset.z)
//
//        target.set(
//            position
//                .cpy()
//                .add(tmpVector)
//        )
//        tmpVector.setZero()
//        tmpVector
//            .set(motionState.position)
//        perspectiveCamera.position.lerp(target, 0.8f)
//        cameraDirection.set(position)

        perspectiveCamera.lookAt(position + motionState.forward * 2f)
        perspectiveCamera.up.set(Vector3.Y)
        perspectiveCamera.update()
    }
}
