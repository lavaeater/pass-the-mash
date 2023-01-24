package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.MathUtils
import depth.ecs.components.Camera3dFollowComponent
import depth.ecs.components.Direction
import depth.ecs.components.Rotation
import eater.input.KeyPress
import eater.input.command
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import mash.bullet.FrontBack
import mash.ecs.components.BulletVehicleComponent
import mash.ecs.components.KeyboardControlComponent


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
            {  },
            { cameraFollowComponent.offsetY += 0.1f }
        )
        setBoth(
            Keys.DOWN,
            "Down",
            {  },
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

    private val steeringIncrement = 45f
    private var steering = 0f
    private val steeringClamp = 45f
    override fun processEntity(entity: Entity, deltaTime: Float) {

        var engineForce = 0f
        var brakeForce = 0f

        if (controlComponent.has(Rotation.YawLeft)) {
            steering += deltaTime * steeringIncrement
        }

        if (controlComponent.has(Rotation.YawRight)) {
            steering -= deltaTime * steeringIncrement
        }

        steering = MathUtils.clamp(steering, -steeringClamp, steeringClamp)

        val vehicle = BulletVehicleComponent.get(entity).bulletVehicle
        vehicle.setSteering(steering)

        if (controlComponent.has(Direction.Forward)) {
            vehicle.applyEngineForce(1000f)
        }
        if (controlComponent.has(Direction.Reverse)) {
            vehicle.applyBrakeForce(100f)
        }
    }
}
