package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Quaternion
import depth.ecs.components.MotionStateComponent
import ktx.ashley.allOf
import mash.ecs.components.BulletVehicleComponent

class KeepCarFromFlippingSystem :
    IteratingSystem(allOf(BulletVehicleComponent::class, MotionStateComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {


        val motionState = MotionStateComponent.get(entity).motionState

        /*

        Now we can simply check the rotations of the vehicle and stop certain rotations or apply some force
        I guess

        so, we can check the UP vector against... the standard Z vector, I guess...
         */

        val someRotation = motionState.transform.getRotation(Quaternion())
        stopTheFlip(
            entity,
            (someRotation.pitch > 20f || someRotation.pitch < -20f),
            (someRotation.roll > 20f || someRotation.roll < -20f)
        )
    }

    private fun stopTheFlip(entity: Entity, stopPitch: Boolean, stopRoll: Boolean) {
        if(!stopPitch && !stopRoll)
            return

        val bulletVehicleComponent = BulletVehicleComponent.get(entity)
        val bulletVehicle = bulletVehicleComponent.bulletVehicle
        val rigidBody = bulletVehicle.bulletBody
        if(stopPitch) {
            val angularSomething = rigidBody.angularVelocity
            //Apply an impulse to stop the angular velocity, I guess?
        }
    }

}