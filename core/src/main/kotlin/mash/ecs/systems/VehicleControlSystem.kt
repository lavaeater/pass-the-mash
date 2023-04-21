package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.MathUtils
import ecs.components.Camera3dFollowComponent
import general.Direction
import general.Rotation
import eater.input.KeyPress
import eater.input.command
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import ecs.components.BulletVehicleComponent
import ecs.components.KeyboardControlComponent


class VehicleControlSystem :
    IteratingSystem(
        allOf(
            KeyboardControlComponent::class,
            BulletVehicleComponent::class
        ).get()
    ),
    KtxInputAdapter {
    private val family = allOf(KeyboardControlComponent::class).get()

    private val controlledEntity get() = engine.getEntitiesFor(family).first()

    private val controlComponent get() = KeyboardControlComponent.get(controlledEntity)
    private val cameraFollowComponent get() = Camera3dFollowComponent.get(controlledEntity)

    private val controlMap = command("Controoool") {
        setBoth(
            Keys.W,
            "Throttle",
            { controlComponent.remove(Direction.Forward) },
            { controlComponent.add(Direction.Forward) }
        )
        setBoth(
            Keys.S,
            "Brake",
            { controlComponent.remove(Direction.Reverse) },
            { controlComponent.add(Direction.Reverse) }
        )
        setBoth(
            Keys.A,
            "Left",
            { controlComponent.remove(Rotation.YawLeft) },
            { controlComponent.add(Rotation.YawLeft) }
        )
        setBoth(
            Keys.D,
            "Right",
            { controlComponent.remove(Rotation.YawRight) },
            { controlComponent.add(Rotation.YawRight) }
        )
        setBoth(
            Keys.UP,
            "Up",
            { },
            { cameraFollowComponent.offsetY += 0.1f }
        )
        setBoth(
            Keys.DOWN,
            "Down",
            { },
            { cameraFollowComponent.offsetY -= 0.1f }
        )
        setBoth(
            Keys.LEFT,
            "Up",
            { },
            { cameraFollowComponent.offsetXZ.rotateDeg(5f) }
        )
        setBoth(
            Keys.RIGHT,
            "Down",
            { },
            { cameraFollowComponent.offsetXZ.rotateDeg(-5f) }
        )
    }

    override fun keyDown(keycode: Int): Boolean {
        return controlMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return controlMap.execute(keycode, KeyPress.Up)
    }

    override fun update(deltaTime: Float) {
        Gdx.input.inputProcessor = this
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val vc = BulletVehicleComponent.get(entity)
        val vehicle = vc.bulletVehicle

        val vehicleParams = vehicle.vehicleParams
        if (controlComponent.has(Rotation.YawLeft)) {
            vc.currentAngle += deltaTime * vehicleParams.steeringSpeed
        } else if (controlComponent.has(Rotation.YawRight)) {
            vc.currentAngle -= deltaTime * vehicleParams.steeringSpeed
        } else {
            vc.currentAngle = 0f // for now
        }

        vc.currentAngle = MathUtils.clamp(vc.currentAngle, -vehicleParams.maxSteerAngle, vehicleParams.maxSteerAngle)

        vehicle.setSteeringDeg(vc.currentAngle)

        if (controlComponent.has(Direction.Forward)) {
            vc.currentForce += vehicleParams.acceleration * deltaTime
            vc.currentForce = MathUtils.clamp(vc.currentForce, 0f, vehicleParams.maxForce)
        } else {
            vc.currentForce = 0f
        }
        vehicle.applyEngineForce(vc.currentForce)
        if (controlComponent.has(Direction.Reverse)) {
            vehicle.applyBrakeForce(vehicleParams.brakeForce * deltaTime)
        }

//        for (i in vehicle.wheelIndices.values) {
//            vehicle.vehicle.updateWheelTransform(i, true)
//        }
    }
}
