package depth.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import depth.ecs.components.BulletRigidBody
import depth.ecs.components.Camera3dFollowComponent
import depth.ecs.components.MotionState
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import ktx.math.vec3

class UpdatePerspectiveCameraSystem(
    private val perspectiveCamera: PerspectiveCamera
) :
    IteratingSystem(
        allOf(
            MotionState::class,
            Camera3dFollowComponent::class
        ).get()
    ) {

    val target = vec3()
    val cameraDirection = vec3()
    val tmpVector = vec3()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val motionState = MotionState.get(entity)

        val position = motionState.position
        val cc = Camera3dFollowComponent.get(entity)

        perspectiveCamera.position.set(tmpVector.set(position).add(cc.offsetXZ.x, cc.offsetY, cc.offsetXZ.y))

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

        perspectiveCamera.lookAt(position)
        perspectiveCamera.up.set(Vector3.Y)
        perspectiveCamera.update()
    }
}
