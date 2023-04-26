package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.vec3
import threedee.ecs.components.*
import threedee.ecs.systems.plus
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
    private val animationController by lazy { Animation3dComponent.get(controlledEntity).animationController }
    private val rigidBodyComponent by lazy { BulletRigidBody.get(controlledEntity) }
    private val rigidBody by lazy { rigidBodyComponent.rigidBody }
    private val motionStateComponent by lazy { MotionStateComponent.get(controlledEntity) }

    private val controlMap = command("Controoool") {
        setBoth(
            Input.Keys.W,
            "Throttle",
            { controlComponent.remove(Direction.Forward) },
            { controlComponent.add(Direction.Forward) }
        )
        setBoth(
            Input.Keys.S,
            "Brake",
            { controlComponent.remove(Direction.Reverse) },
            { controlComponent.add(Direction.Reverse) }
        )
        setBoth(
            Input.Keys.A,
            "Left",
            { controlComponent.remove(Direction.Left) },
            { controlComponent.add(Direction.Left) }
        )
        setBoth(
            Input.Keys.D,
            "Right",
            { controlComponent.remove(Direction.Right) },
            { controlComponent.add(Direction.Right) }
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
        var needsAnimInit = true
        val animKeys = mutableListOf<String>()
        var currentAnimIndex = 0
        var maxIndex = 0
        setDown(Input.Keys.SPACE, "Toggle Animation") {
            if (needsAnimInit) {
                needsAnimInit = false
                animKeys.addAll(Animation3dComponent.get(controlledEntity).animations.map { it.id })
                maxIndex = animKeys.lastIndex
                currentAnimIndex = animKeys.indexOf(animationController.current.animation.id)
            }
            currentAnimIndex++
            if (currentAnimIndex > maxIndex) {
                currentAnimIndex = 0
            }
            animationController.setAnimation(animKeys[currentAnimIndex], -1, 0.75f, null)
        }
//        setBoth(
//            Input.Keys.UP,
//            "Up",
//            { },
//            { cameraFollowComponent.offsetY += 0.1f }
//        )
//        setBoth(
//            Input.Keys.DOWN,
//            "Down",
//            { },
//            { cameraFollowComponent.offsetY -= 0.1f }
//        )
//        setBoth(
//            Input.Keys.LEFT,
//            "Up",
//            { },
//            { cameraFollowComponent.offsetXZ.rotateDeg(5f) }
//        )
//        setBoth(
//            Input.Keys.RIGHT,
//            "Down",
//            { },
//            { cameraFollowComponent.offsetXZ.rotateDeg(-5f) }
//        )
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


    val worldPosition = vec3()
    val rotationDirection = Vector3.X
    val towardsCameraVector = vec3(-1f, 0f, 1f)
    override fun processEntity(entity: Entity, deltaTime: Float) {
        setDirectionVector(controlComponent.directionControl)
        if (directionVector.isZero) {
            rotationDirection.lerp(towardsCameraVector, 0.5f)
        } else {
            rotationDirection.lerp(directionVector, 0.2f)
        }
        scene.modelInstance.transform.getTranslation(worldPosition)
        worldPosition.lerp((worldPosition + directionVector), 0.1f)
        scene.modelInstance.transform.setToWorld(worldPosition, Vector3.Z, Vector3.Y)
        scene.modelInstance.transform.rotateTowardDirection(rotationDirection, Vector3.Y)

        rigidBody.worldTransform = scene.modelInstance.transform
        //motionStateComponent.motionState.setWorldTransform(scene.modelInstance.transform)
    }
}