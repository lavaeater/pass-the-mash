package depth.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import twodee.input.KeyPress
import twodee.input.command
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import ktx.math.vec3
import threedee.ecs.components.*
import threedee.general.Direction
import threedee.general.Rotation


class EntityControlSystem :
    IteratingSystem(
        allOf(
            CharacterControlComponent::class,
            MotionStateComponent::class,
            SceneComponent::class
        ).get()
    ),
    KtxInputAdapter {
    private val family = allOf(CharacterControlComponent::class).get()

    private val controlledEntity get() = engine.getEntitiesFor(family).first()

    private val controlComponent get() = CharacterControlComponent.get(controlledEntity)
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

    private val forceFactor = 10f
    private val torqueFactor = 0.1f
    private val tmpVector = vec3()
    private val centralForce = vec3()

    override fun update(deltaTime: Float) {
        Gdx.input.inputProcessor = this
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidBody = BulletRigidBody.get(entity).rigidBody
        val motionState = MotionStateComponent.get(entity)

        if (controlComponent.has(Rotation.YawLeft)) {
            rigidBody.applyTorqueImpulse(vec3(0f, torqueFactor, 0f))
        }

        if (controlComponent.has(Rotation.YawRight)) {
            rigidBody.applyTorqueImpulse(vec3(0f, -torqueFactor, 0f))
        }

        centralForce.setZero()
        if (controlComponent.has(Direction.Left)) {
            tmpVector.setZero()
            tmpVector.set(motionState.right).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        if (controlComponent.has(Direction.Right)) {
            tmpVector.setZero()
            tmpVector.set(motionState.left).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        if (controlComponent.has(Direction.Up)) {
            tmpVector.setZero()
            tmpVector.set(motionState.up).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        if (controlComponent.has(Direction.Down)) {
            tmpVector.setZero()
            tmpVector.set(motionState.down).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        if (controlComponent.has(Direction.Forward)) {
            tmpVector.setZero()
            tmpVector.set(motionState.forward).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        if (controlComponent.has(Direction.Reverse)) {
            tmpVector.setZero()
            tmpVector.set(motionState.backwards).scl(forceFactor)
            centralForce.add(tmpVector)
        }
        rigidBody.applyCentralImpulse(centralForce)
    }
}
