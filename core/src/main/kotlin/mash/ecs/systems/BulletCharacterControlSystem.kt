package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Plane
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.vec3
import threedee.ecs.components.*
import threedee.ecs.systems.inXZPlane
import threedee.general.Direction
import threedee.general.DirectionControl
import twodee.injection.InjectionContext.Companion.inject
import twodee.input.KeyPress
import twodee.input.command

class BulletCharacterControlSystem :
    IteratingSystem(
        allOf(
            IsometricCameraFollowComponent::class,
            KeyboardControlComponent::class,
            SceneComponent::class,
            BulletRigidBody::class
        ).get()
    ),
    KtxInputAdapter {
    private val controlledEntity by lazy { entities.first() }
    private val controlComponent by lazy { KeyboardControlComponent.get(controlledEntity) }
    private val scene by lazy { SceneComponent.get(controlledEntity).scene }
    private val camera by lazy { inject<OrthographicCamera>() }
    private val characterStateMachineComponent by lazy { CharacterAnimationStateComponent.get(controlledEntity) }
    private val rigidBodyComponent by lazy { BulletRigidBody.get(controlledEntity) }
    private val rigidBody by lazy { rigidBodyComponent.rigidBody }

    private val controlMap = command("Controoool") {
        setBoth(
            Input.Keys.W,
            "WalkForward",
            {
                controlComponent.remove(Direction.Forward)
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            },
            {
                controlComponent.add(Direction.Forward)
                characterStateMachineComponent.acceptEvent(CharacterEvent.MoveForwards)
            }
        )
        setBoth(
            Input.Keys.S,
            "Brake",
            {
                controlComponent.remove(Direction.Reverse)
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            },
            {
                controlComponent.add(Direction.Reverse)
                characterStateMachineComponent.acceptEvent(CharacterEvent.MoveBackwards)
            }
        )
        setBoth(
            Input.Keys.A,
            "Left",
            {
                controlComponent.remove(Direction.Left)
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            },
            {
                controlComponent.add(Direction.Left)
                characterStateMachineComponent.acceptEvent(CharacterEvent.StrafeLeft)

            }
        )
        setBoth(
            Input.Keys.D,
            "Right",
            {
                controlComponent.remove(Direction.Right)
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            },
            {
                controlComponent.add(Direction.Right)
                characterStateMachineComponent.acceptEvent(CharacterEvent.StrafeRight)
            }
        )
        setDown(Input.Keys.UP, "zOom in") {
            camera.zoom /= 1.1f
            info { "zoom: ${camera.zoom}" }
            camera.update()
        }
        setDown(Input.Keys.DOWN, "zOom out") {
            camera.zoom *= 1.1f
            info { "zoom: ${camera.zoom}" }
            camera.update()
        }
        setDown(Input.Keys.NUMPAD_8, "zOom out") {
            camera.near *= 1.25f
            info { "near: ${camera.near}" }
            camera.update()
        }
        setDown(Input.Keys.NUMPAD_2, "zOom out") {
            camera.near /= 1.25f
            info { "near: ${camera.near}" }
            camera.update()
        }
        setDown(Input.Keys.NUMPAD_6, "zOom out") {
            camera.far *= 1.25f
            info { "far: ${camera.far}" }
            camera.update()
        }
        setDown(Input.Keys.NUMPAD_4, "zOom out") {
            camera.far /= 1.25f
            info { "far: ${camera.far}" }
            camera.update()
        }
        setDown(Input.Keys.CONTROL_LEFT, "Toggle Crawl") {
            if (characterStateMachineComponent.currentState == CharacterState.LowCrawling) {
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            } else {
                characterStateMachineComponent.acceptEvent(CharacterEvent.StartLowCrawl)
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        return controlMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return controlMap.execute(keycode, KeyPress.Up)
    }

    var mouseScreenPosition = vec3()
        get() {
            field.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            return field
        }
        private set

    var mousePosition = vec3()
        get() {
            field.set(mouseScreenPosition)
            return camera.unproject(field)
        }
        private set

    override fun update(deltaTime: Float) {
        Gdx.input.inputProcessor = this
        controlComponent.mouseWorldPosition.set(mousePosition)
        super.update(deltaTime)
    }

    private val directionToVector = mapOf(
        Direction.Forward to Vector3(1f, 0f, -1f),
        Direction.Reverse to Vector3(-1f, 0f, 1f),
        Direction.Left to Vector3(-1f, 0f, -1f),
        Direction.Right to Vector3(1f, 0f, 1f)
    )

    private val directionVector = vec3()
    private fun setDirectionVector(directionControl: DirectionControl) {
        if (directionControl.orthogonal.isEmpty()) {
            directionVector.setZero()
            return
        }
        directionControl.orthogonal.forEach { directionVector.add(directionToVector[it]!!) }
        directionVector.nor()
    }

    /***
     *
     * Ah, directions are moore complex than I thought. First off,
     * if the direction control has forwards or backwards, that means we are moving in those
     * directions, but the rotation direction is TOWARDS mouse.
     * This method sets the direction vector to a vector that is in 90 degree increments
     */
    private val rotate = false
    private val otherNeckNode by lazy { scene.modelInstance.getNode("mixamorig:LeftUpLeg") }

    private val worldPosition = vec3()
    private val rotationDirection = Vector3.X.cpy()
    private val towardsCameraVector = vec3(-1f, 0f, 1f)
    private var angle = 1f
    private var logCooldown = 1f

    private val ray = Ray(vec3(), vec3())
    private val plane = Plane(vec3(0f, 1f, 0f), 0f)
    private var intersection = vec3()
        get() {
            if(Intersector.intersectRayPlane(ray, plane, field))
                return field
            return Vector3.Zero
        }

    val desiredOrientation = Quaternion()
    val torque = Quaternion()
    val torqueMagnitude = 10f

    override fun processEntity(entity: Entity, deltaTime: Float) {
        logCooldown -= deltaTime

        val msComponent = MotionStateComponent.get(entity)
        worldPosition.set(msComponent.position)
        ray.set(mousePosition, camera.direction)
        plane.set(msComponent.position, Vector3.Y)

        rotationDirection.set(intersection).sub(worldPosition).nor()
        desiredOrientation.setFromCross(msComponent.forward, rotationDirection.inXZPlane()).nor()

        torque.set(desiredOrientation).mulLeft(msComponent.motionState.currentOrientation.conjugate()).nor()

        rigidBody.applyTorque(vec3(torque.x * torqueMagnitude, torque.y * torqueMagnitude, torque.z * torqueMagnitude))

        if (logCooldown < 0f) {
            logCooldown = 1f
            info { "worldPosition: ${worldPosition.inXZPlane()} \n" }
            info { "mousePosition: ${mousePosition.inXZPlane()} \n" }
            info { "rotationDirection: $rotationDirection\n" }
        }

//        rigidBody.worldTransform.rotateTowardDirection(rotationDirection.inXZPlane(), Vector3.Y)

        //scene.modelInstance.transform.rotateTowardDirection(rotationDirection, Vector3.Y)





//        scene.modelInstance.transform.rotate(worldPosition, mousePosition)

        //rotate directionVector to in rotation direction I guess?

//        worldPosition.lerp((worldPosition + directionVector), 0.1f)
//        scene.modelInstance.transform.setToWorld(worldPosition, Vector3.Z, Vector3.Y)

//        rigidBody.worldTransform = scene.modelInstance.transform

//        if (rotate) {
//            val currentRotation = otherNeckNode.rotation.cpy()
//            if (currentRotation.pitch > 45f || currentRotation.pitch < -45f)
//                angle = -angle
//            currentRotation.setEulerAngles(currentRotation.yaw, currentRotation.pitch + angle, currentRotation.roll)
//            info { currentRotation.pitch.toString() }
//            otherNeckNode.rotation.set(currentRotation)
//            scene.modelInstance.calculateTransforms()
//        }

    }
}
