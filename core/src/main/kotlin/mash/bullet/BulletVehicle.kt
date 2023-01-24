package mash.bullet

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.dynamics.*
import ktx.log.info
import ktx.math.vec3


sealed class LeftRight {
    object Left : LeftRight()
    object Right : LeftRight()
}

sealed class FrontBack {
    object Front : FrontBack()
    object Back : FrontBack()
}

sealed class WheelPosition(val leftOrRight: LeftRight, val frontOrBack: FrontBack) {
    val isFrontWheel get() = frontOrBack == FrontBack.Front

    object FrontLeft : WheelPosition(LeftRight.Left, FrontBack.Front)
    object FrontRight : WheelPosition(LeftRight.Right, FrontBack.Front)
    object BackLeft : WheelPosition(LeftRight.Left, FrontBack.Back)
    object BackRight : WheelPosition(LeftRight.Right, FrontBack.Back)

    companion object {
        val directions = listOf(FrontLeft, FrontRight, BackLeft, BackRight)
    }
}

class BulletVehicle(
    val raycaster: btDefaultVehicleRaycaster,
    val tuning: btRaycastVehicle.btVehicleTuning,
    val localInertia: Vector3,
    val boundingBox: BoundingBox,
    val bulletBody: btRigidBody,
    val vehicle: btRaycastVehicle
) {
    val wheels = mutableMapOf<WheelPosition, btWheelInfo>()
    val chassisHalfExtents = boundingBox.getDimensions(vec3()).scl(0.5f)
    val wheelIndexes = mutableMapOf<WheelPosition, Int>()
    var wheelIndex = 0

    fun addWheel(position: WheelPosition, wheelDimensions: Vector3) {
        val wheelHalfExtents = wheelDimensions.cpy().scl(0.5f)
        val point = Vector3()
        val direction = Vector3(0f, -1f, 0f) //Oh, so the wheel should point DOWN of all things?
        val axis = Vector3(-1f, 0f, 0f) // Why is the axis x: -1?

        wheels[position] = vehicle
            .addWheel(
                point.set(chassisHalfExtents)
                    .scl(
                        if (position.leftOrRight is LeftRight.Left) 0.9f else -0.9f,
                        -0.8f,
                        if(position.frontOrBack is FrontBack.Front) 0.7f else -0.5f
                    ),
                direction,
                axis,
                wheelHalfExtents.z * 0.3f,
                wheelHalfExtents.z,
                tuning,
                position.isFrontWheel
            )
        wheelIndexes[position] = wheelIndex
        wheelIndex++
    }

    fun setSteering(steering: Float) {
        for(wheelIndex in wheelIndexes.filterKeys { it.isFrontWheel }.values) {
            vehicle.setSteeringValue(steering, wheelIndex)
        }
    }

    fun applyEngineForce(engineForce: Float) {
        for(wheelIndex in wheelIndexes.filterKeys { !it.isFrontWheel }.values) {
            info { "Applying force $engineForce to wheel $wheelIndex" }
            vehicle.applyEngineForce(engineForce, wheelIndex)
        }
    }

    fun applyBrakeForce(brakeForce: Float) {
        for (wheelIndex in wheelIndexes.filterKeys { !it.isFrontWheel }.values) {
            info { "Applying brake $brakeForce to wheel $wheelIndex" }
            vehicle.setBrake(brakeForce, wheelIndex)
        }
    }

    companion object {
        /**
         * Creates and adds a vehicle with the given parameters to the dynamicsworld.
         *
         *
         */
        fun createVehicle(
            shape: btCollisionShape,
            boundingBox: BoundingBox,
            mass: Float,
            dynamicsWorld: btDynamicsWorld
        ): BulletVehicle {
            val raycaster = btDefaultVehicleRaycaster(dynamicsWorld)
            val tuning = btRaycastVehicle.btVehicleTuning()
            val localInertia = vec3()
            shape.calculateLocalInertia(mass, localInertia)

            val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia)
            val bulletBody = btRigidBody(bodyInfo)
                .apply {
                Collision.DISABLE_DEACTIVATION
            }
            val vehicle = btRaycastVehicle(tuning, bulletBody, raycaster)
            dynamicsWorld.addVehicle(vehicle)
            dynamicsWorld.addRigidBody(bulletBody)
            vehicle.setCoordinateSystem(0, 1, 2)
            return BulletVehicle(raycaster, tuning, localInertia, boundingBox, bulletBody, vehicle)
        }
    }
}