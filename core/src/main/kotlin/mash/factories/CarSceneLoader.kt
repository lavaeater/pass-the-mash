package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.dynamics.*
import depth.ecs.components.*
import eater.core.engine
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.math.div
import ktx.math.vec3
import mash.ecs.components.KeyboardControlComponent
import mash.ecs.components.BulletVehicle
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil


class CarSceneLoader : SceneLoader() {
    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        /**
         * Is it reasonable to just load resources here that we just might only need
         * for this particular scene?
         */
        setUpScene(sceneManager)
        createFloor(1000f, 0f, 1000f, sceneManager, dynamicsWorld)
        loadCar(sceneManager, dynamicsWorld)
    }

    override fun setupEnvironment(sceneManager: SceneManager) {
        val environmentCubemap = EnvironmentUtil.createCubemap(
            InternalFileHandleResolver(),
            "textures/environment/environment_", ".png", EnvironmentUtil.FACE_NAMES_NEG_POS
        ).alsoRegister()
        val diffuseCubemap = EnvironmentUtil.createCubemap(
            InternalFileHandleResolver(),
            "textures/diffuse/diffuse_", ".png", EnvironmentUtil.FACE_NAMES_NEG_POS
        ).alsoRegister()
        val specularCubemap = EnvironmentUtil.createCubemap(
            InternalFileHandleResolver(),
            "textures/specular/specular_", "_", ".png", 10, EnvironmentUtil.FACE_NAMES_NEG_POS
        ).alsoRegister()
        val brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png")).alsoRegister()

        sceneManager.setAmbientLight(1f)
        sceneManager.environment.set(PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT))
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))
        sceneManager.environment.apply {
            add(DirectionalShadowLight().apply {
                set(1f, 0.5f, 1f, -1f, -1f, 0f)
            }.alsoRegister())
        }
        // setup skybox
        sceneManager.skyBox = SceneSkybox(environmentCubemap).alsoRegister()
    }

    fun loadCar(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val someCar = GLTFLoader().load(Gdx.files.internal("models/my-cars/delivery-van.gltf")).alsoRegister()

        val carScene = Scene(someCar.scene).apply {
            this.modelInstance.transform.setToWorld(
                vec3(0f, 2f, 0f), Vector3.Z, Vector3.Y
            )
        }

        val boundingBox = BoundingBox()
        carScene.modelInstance.model.calculateBoundingBox(boundingBox)

        val carShape = btBoxShape(boundingBox.getDimensions(vec3()) / 2f).alsoRegister()
        // Create the vehicle
        val raycaster = btDefaultVehicleRaycaster(dynamicsWorld)
        val tuning = btRaycastVehicle.btVehicleTuning()
        val motionState = MotionState()
        val localInertia = vec3()
        val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(10f, motionState, carShape, localInertia)
        val bulletBody = btRigidBody(bodyInfo)
        val vehicle = btRaycastVehicle(tuning, bulletBody, raycaster)
        bulletBody.activationState = Collision.DISABLE_DEACTIVATION
        dynamicsWorld.addVehicle(vehicle)
        vehicle.setCoordinateSystem(0, 1, 2)
        lateinit var wheelInfo: btWheelInfo
        val point = Vector3()
        val direction = Vector3(0f, -1f, 0f)
        val axis = Vector3(-1f, 0f, 0f)
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(0.9f, -0.8f, 0.7f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, true
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(-0.9f, -0.8f, 0.7f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, true
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(0.9f, -0.8f, -0.5f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, false
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(-0.9f, -0.8f, -0.5f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, false
        )






        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = carScene
                sceneManager.addScene(carScene)
            }
            lateinit var motionState: MotionState
            lateinit var bulletBody: btRigidBody
            with<MotionState> {
                motionState = this
                transform = carScene.modelInstance.transform
            }
            with<Camera3dFollowComponent>()
            with<BulletRigidBody> {
                carShape.calculateLocalInertia(10f, localInertia)

                rigidBody = bulletBody
                dynamicsWorld.addRigidBody(bulletBody)
            }
            with<BulletVehicle> {
                val rcv = btDefaultVehicleRaycaster(dynamicsWorld).alsoRegister()
                val tuner = btRaycastVehicle.btVehicleTuning().alsoRegister()
                val actualVehicle = btRaycastVehicle(btRaycastVehicle.btVehicleTuning(), bulletBody, rcv).alsoRegister()
                //Front Right wheel
                actualVehicle.addWheel(
                    vec3(0.5f,0f,0f),
                    vec3(1f, 0f, 0f),
                    Vector3.Z, 3f, 1f, tuner, false).alsoRegister()
                actualVehicle.addWheel(
                    vec3(-0.5f,0f,0f),
                    vec3(1f, 0f, 0f),
                    Vector3.Z, 3f, 1f, tuner, false).alsoRegister()
                //Front left wheel
//                actualVehicle.addWheel(
//                    vec3(-1f,0f,-1f),
//                    vec3(1f, 0f, -1f),
//                    Vector3.Y, 1f, 1f, tuner, true).alsoRegister()

                //Back Right wheel
//                actualVehicle.addWheel(
//                    vec3(1f,0f,1f),
//                    vec3(0f, 0f, -1f),
//                    Vector3.X, 1f, 1f, tuner, false).alsoRegister()
//
//                //Back left wheel.
//                actualVehicle.addWheel(
//                    vec3(1f,0f,1f),
//                    vec3(0f, 0f, -1f),
//                    Vector3.X, 1f, 1f, tuner, false).alsoRegister()

                bulletVehicle = actualVehicle
                dynamicsWorld.addVehicle(actualVehicle)
            }
            with<KeyboardControlComponent>()
        }
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}