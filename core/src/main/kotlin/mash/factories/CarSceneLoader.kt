package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.*
import depth.ecs.components.*
import eater.core.engine
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.math.vec3
import mash.bullet.BulletVehicle
import mash.bullet.WheelPosition
import mash.core.getBoundingBox
import mash.core.getBoxShape
import mash.core.loadModel
import mash.ecs.components.KeyboardControlComponent
import mash.ecs.components.BulletVehicleComponent
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
        createFloor(1000f, 1f, 1000f, sceneManager, dynamicsWorld)
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
        val someCar = "models/my-cars/delivery-van.gltf".loadModel().alsoRegister()

        val carScene = Scene(someCar.scene)
            .apply {
            this.modelInstance.transform.setToWorld(
                vec3(0f, 2.5f, 0f), Vector3.Z, Vector3.Y
            )
        }

        val boundingBox = carScene.getBoundingBox()
        val carShape = boundingBox.getBoxShape().alsoRegister()
        // Create the vehicle
        val bv = BulletVehicle.createVehicle(carShape, boundingBox, 5f, dynamicsWorld)
        for (direction in WheelPosition.directions) {
            bv.addWheel(direction, vec3(0.5f, 0.5f, 0.5f))
        }

        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = carScene
                sceneManager.addScene(carScene)
            }
            with<Camera3dFollowComponent>()
            with<BulletRigidBody> {
                rigidBody = bv.bulletBody
            }
            with<BulletVehicleComponent> {
                bulletVehicle = bv
            }
            with<KeyboardControlComponent>()
            with<MotionStateComponent> {
                val ms = MotionState(carScene.modelInstance.transform)
                motionState = ms
                bv.bulletBody.motionState = ms
            }
        }
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}

