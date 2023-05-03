package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.math.vec3
import mash.core.getBoundingBox
import mash.core.getBoxShape
import mash.core.loadModel
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil
import threedee.bullet.MotionState
import threedee.ecs.components.*
import twodee.core.engine

class CarSceneLoader(val trackGenerator: TrackGenerator) : SceneLoader() {
    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        /**
         * Is it reasonable to just load resources here that we just might only need
         * for this particular scene?
         */
        setUpScene(sceneManager)
        loadTrack(sceneManager, dynamicsWorld)
        //createFloor(1000f, 1f, 1000f, sceneManager, dynamicsWorld)
        loadCar(sceneManager, dynamicsWorld)
    }

    private fun loadTrack(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val track = trackGenerator.generateTrack()
        sceneManager.addScene(track.scene)
        dynamicsWorld.addRigidBody(track.body)
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
                set(1f, 1f, 0f, -1f, -1f, 0f)
            }.alsoRegister())
        }

        sceneManager.environment.apply {
            add(DirectionalShadowLight().apply {
                set(1f, 1f, 1f, 0f, -1f, 1f)
            }.alsoRegister())
        }
        // setup skybox
        sceneManager.skyBox = SceneSkybox(environmentCubemap).alsoRegister()
    }

    fun loadCar(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val someCar = "models/cars/ambulance.glb".loadModel().alsoRegister()

        val carScene = Scene(someCar.scene)
            .apply {
            this.modelInstance.transform.setToWorld(
                vec3(0f, 15f, -5f), Vector3.Z, Vector3.Y
            )
        }

        val boundingBox = carScene.getBoundingBox()
        val carShape = boundingBox.getBoxShape().alsoRegister()

        // Create the vehicle
        val bv = threedee.bullet.BulletVehicle.createVehicle(
            carShape,
            boundingBox,
            5f,
            dynamicsWorld,
            100f,
            25f,
            45f,
            20f,
            100f)



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
            with<CharacterControlComponent>()
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

