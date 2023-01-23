package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
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
                vec3(0f, 0f, 0f), Vector3.Z, Vector3.Y
            )
        }

        val boundingBox = BoundingBox()
        carScene.modelInstance.model.calculateBoundingBox(boundingBox)

        val carShape = btBoxShape(boundingBox.getDimensions(vec3()) / 2f).alsoRegister()

//        val modelVertexArray = btTriangleIndexVertexArray(carScene.modelInstance.model.meshParts)
//        val carShape = btRigidBody.btRigidBodyConstructionInfo() btGImpactMeshShape(modelVertexArray)

        //I have to fix this
//        carShape.localScaling = Vector3(1f, 1f, 1f)
//        carShape.margin = 1f
        val localInertia = vec3()

//        carShape.calculateLocalInertia(1f, localInertia)

//        val boundingBox = BoundingBox()
//        carScene.modelInstance.calculateBoundingBox(boundingBox)
//        val dimensions = vec3(
//            boundingBox.width / 2f,
//            boundingBox.height / 2f,
//            boundingBox.height / 2f
//        )

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
            with<Camera3dFollowComponent> {
                distance = 0f
                offset.set(3f, 3f, -3f)
            }
            with<BulletRigidBody> {
                carShape.calculateLocalInertia(10f, localInertia)
                val info = btRigidBody.btRigidBodyConstructionInfo(10f, motionState, carShape, localInertia)
                bulletBody = btRigidBody(info).apply {
                    setDamping(0.5f, 0.5f)
                    angularFactor = Vector3.Y
                }
                rigidBody = bulletBody
                dynamicsWorld.addRigidBody(bulletBody)
            }
            with<BulletVehicle> {
                val rcv = btDefaultVehicleRaycaster(dynamicsWorld).alsoRegister()
                val tuner = btRaycastVehicle.btVehicleTuning().alsoRegister()
                val actualVehicle = btRaycastVehicle(btRaycastVehicle.btVehicleTuning(), bulletBody, rcv).alsoRegister()
                //Front Right wheel
                actualVehicle.addWheel(
                    vec3(1f,0f,-1f),
                    vec3(0f, 0f, -1f),
                    Vector3.X, 3f, 1f, tuner, false).alsoRegister()
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