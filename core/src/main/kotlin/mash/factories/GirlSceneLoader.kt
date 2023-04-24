package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.collections.toGdxArray
import ktx.math.vec3
import mash.core.getBoundingBox
import mash.core.getBoxShape
import mash.core.loadModel
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil
import threedee.bullet.MotionState
import threedee.ecs.components.*
import twodee.core.engine
import twodee.ecs.ashley.components.Player

class GirlSceneLoader : SceneLoader() {
    val anims = listOf("idle", "idle-2", "idle-3", "idle-4", "idle-5", "walking", "jump-up", "hard-landing")


    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        setUpScene(sceneManager)
        createFloor(100f, 1f, 100f, sceneManager, dynamicsWorld)
        loadGirl(sceneManager, dynamicsWorld)
    }

    fun loadGirl(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val girlAsset = "models/girl-gltf/run.glb".loadModel().alsoRegister()
        girlAsset.animations.first().id = "run"

        val loadedAnims = anims.map {
            val m = "models/girl-gltf/$it.glb".loadModel()
            val s = m.animations.toList()
            s.forEach { animation ->
                animation.id = it
            }
            s
        }.flatten().toGdxArray()

        girlAsset.scene.model.animations.addAll(loadedAnims)
        girlAsset.animations.addAll(loadedAnims)

        val girlScene = Scene(girlAsset.scene)
            .apply {
                this.modelInstance.transform.setToWorld(
                    vec3(0f, 0.1f, 0f), Vector3.Z, Vector3.Y
                )
                this.modelInstance.transform.rotate(Quaternion().apply {
                    setEulerAngles(0f, -90f, 0f)
                })
            }

//        val boundingBox = girlScene.getBoundingBox()
//        val girlShape = boundingBox.getBoxShape().alsoRegister()
//        val localInertia = vec3()
//        val mass = 5f
//        girlShape.calculateLocalInertia(mass, localInertia)
//
//        val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(mass, null, girlShape, localInertia)
//        val girlBody = btRigidBody(bodyInfo).alsoRegister()
//        dynamicsWorld.addRigidBody(girlBody)


        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = girlScene
                sceneManager.addScene(girlScene)
            }
            with<Animation3dComponent> {
                animations = girlAsset.animations
                animationPlayer = girlScene.animations
                animationController = girlScene.animationController
                animationController.setAnimation("run", -1, 1f, null)
            }
//            with<BulletRigidBody> {
//                rigidBody = girlBody
//            }
//            with<MotionStateComponent> {
//                val ms = MotionState(girlScene.modelInstance.transform)
//                motionState = ms
//                girlBody.motionState = ms
//            }
            with<Player>()
            with<IsometricCameraFollowComponent>()
            with<KeyboardControlComponent>()
        }
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
//        sceneManager.skyBox = SceneSkybox(environmentCubemap).alsoRegister()
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}