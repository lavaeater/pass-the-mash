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
import mash.injection.MyContactListener
import threedee.ecs.components.*
import threedee.ecs.systems.plus
import threedee.general.Direction
import threedee.general.CharacterControl
import threedee.general.Modifier
import twodee.injection.InjectionContext.Companion.inject
import twodee.input.KeyPress
import twodee.input.command

class KinematicObjectControlSystem :
    IteratingSystem(
        allOf(
            IsometricCameraFollowComponent::class,
            CharacterControlComponent::class,
            SceneComponent::class,
            KinematicObject::class
        ).get()
    ),
    KtxInputAdapter {
    private val controlledEntity by lazy { entities.first() }
    private val controlComponent by lazy { CharacterControlComponent.get(controlledEntity) }
    private val scene by lazy { SceneComponent.get(controlledEntity).scene }
    private val camera by lazy { inject<OrthographicCamera>() }
    private val kinematicComponent by lazy { KinematicObject.get(controlledEntity) }
    private val kinematicBody by lazy { kinematicComponent.kinematicBody}

    /**
     * This exemplifies the complexities of the animation state.
     *
     * It should really be more like a set of stuff that the character
     * HAS that in turn tell us what animation to play.
     *
     * Like, it has the "crouch" modifier, the we walk in some direction, but
     * crouched.
     *
     * It has "aim" modifier, well, then it moves and crouces at the same time.
     *
     * Make it sooo
     */
    private val mouseButtonCommandMap = command("Mousebuttons") {
        setBoth(Input.Buttons.RIGHT, "Start Aiming",
            {
                controlComponent.remove(Modifier.Aiming)
            }, {
                controlComponent.add(Modifier.Aiming)
            })
    }

    private val keyboardControlMap = command("Controoool") {
        setBoth(
            Input.Keys.W,
            "WalkForward",
            {
                controlComponent.remove(Direction.Forward)
            },
            {
                controlComponent.add(Direction.Forward)
            }
        )
        setBoth(
            Input.Keys.S,
            "Brake",
            {
                controlComponent.remove(Direction.Reverse)
            },
            {
                controlComponent.add(Direction.Reverse)
            }
        )
        setBoth(
            Input.Keys.A,
            "Left",
            {
                controlComponent.remove(Direction.Left)
            },
            {
                controlComponent.add(Direction.Left)
            }
        )
        setBoth(
            Input.Keys.D,
            "Right",
            {
                controlComponent.remove(Direction.Right)
            },
            {
                controlComponent.add(Direction.Right)
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
            controlComponent.toggle(Modifier.Crawling)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        return keyboardControlMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return keyboardControlMap.execute(keycode, KeyPress.Up)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return mouseButtonCommandMap.execute(button, KeyPress.Down)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return mouseButtonCommandMap.execute(button, KeyPress.Up)
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

    private fun setDirectionVector(characterControl: CharacterControl) {
        if (characterControl.orthogonal.isEmpty()) {
            directionVector.setZero()
            return
        }
        characterControl.orthogonal.forEach { directionVector.add(directionToVector[it]!!) }
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

        val kc = CharacterControlComponent.get(entity)
        kc.lookDirection.set(intersect).sub(worldPosition).nor()
        kc.intersection.set(intersect)

        worldPosition.lerp(worldPosition + kc.forward * kc.thrust * 0.2f, 0.5f)
        worldPosition.lerp(worldPosition + kc.left * kc.strafe * 0.2f, 0.5f)

        scene.modelInstance.transform.setToWorld(worldPosition, Vector3.Z, Vector3.Y)
        scene.modelInstance.transform.rotateTowardDirection(kc.lookDirection, Vector3.Y)

        kinematicBody.worldTransform = scene.modelInstance.transform
    }
}
