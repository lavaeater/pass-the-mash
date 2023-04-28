package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Plane
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import ktx.app.KtxInputAdapter
import ktx.ashley.allOf
import ktx.log.info
import ktx.math.times
import ktx.math.vec3
import threedee.ecs.components.*
import threedee.ecs.systems.plus
import threedee.general.Direction
import threedee.general.DirectionControl
import twodee.injection.InjectionContext.Companion.inject
import twodee.input.KeyPress
import twodee.input.command

class BulletGhostObjectControlSystem :
    IteratingSystem(
        allOf(
            IsometricCameraFollowComponent::class,
            KeyboardControlComponent::class,
            SceneComponent::class,
            BulletGhostObject::class
        ).get()
    ),
    KtxInputAdapter {
    private val controlledEntity by lazy { entities.first() }
    private val controlComponent by lazy { KeyboardControlComponent.get(controlledEntity) }
    private val scene by lazy { SceneComponent.get(controlledEntity).scene }
    private val camera by lazy { inject<OrthographicCamera>() }
    private val characterStateMachineComponent by lazy { CharacterAnimationStateComponent.get(controlledEntity) }
    private val ghostComponent by lazy { BulletGhostObject.get(controlledEntity) }
    private val ghostBody by lazy { ghostComponent.ghostObject }

    private val controlMap = command("Controoool") {
        setBoth(
            Input.Keys.W,
            "WalkForward",
            {
                controlComponent.remove(Direction.Forward)
                if(controlComponent.hasNoDirection) {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
                }
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
                if(controlComponent.hasNoDirection) {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
                }
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
                if(controlComponent.hasNoDirection) {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
                }
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
                if(controlComponent.hasNoDirection) {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
                }
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
                if(controlComponent.hasNoDirection) {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
                } else {
                    characterStateMachineComponent.acceptEvent(CharacterEvent.MoveForwards)
                }
                characterStateMachineComponent.acceptEvent(CharacterEvent.Stop)
            } else {
                characterStateMachineComponent.acceptEvent(CharacterEvent.StartCrawling)
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

    private val ray = Ray(vec3(), vec3())
    private val plane = Plane(vec3(0f, 1f, 0f), 0f)
    private val lastIntersection = vec3()
    private val tempIntersection = vec3()
    private val intersect: Vector3
        get() {
            if (Intersector.intersectRayPlane(ray, plane, tempIntersection)) {
                lastIntersection.set(tempIntersection)
            }
            return lastIntersection
        }

    val worldPosition = vec3()
    val rotationVector = Vector3.Z.cpy()
    override fun processEntity(entity: Entity, deltaTime: Float) {

        scene.modelInstance.transform.getTranslation(worldPosition)
        ray.set(mousePosition, camera.direction)

        val kc = KeyboardControlComponent.get(entity)
        kc.lookDirection.set(intersect).sub(worldPosition).nor()
        kc.intersection.set(intersect)

        worldPosition.lerp(worldPosition + kc.forward * kc.thrust * 0.2f, 0.5f)
        worldPosition.lerp(worldPosition + kc.left * kc.strafe * 0.2f, 0.5f)

        scene.modelInstance.transform.setToWorld(worldPosition, Vector3.Z, Vector3.Y)
        scene.modelInstance.transform.rotateTowardDirection(kc.lookDirection, Vector3.Y)

        ghostBody.worldTransform = scene.modelInstance.transform
    }
}
