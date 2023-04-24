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
import mash.core.loadModel
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil
import threedee.ecs.components.Camera3dFollowComponent
import threedee.ecs.components.KeyboardControlComponent
import threedee.ecs.components.SceneComponent
import threedee.ecs.components.VisibleComponent
import twodee.core.engine

class GirlSceneLoader: SceneLoader() {
    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        setUpScene(sceneManager)
        createFloor(100f, 1f, 100f, sceneManager, dynamicsWorld)
        loadGirl(sceneManager, dynamicsWorld)
    }

    fun loadGirl(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val someGirl = "models/girl-gltf/idle/idle.gltf".loadModel().alsoRegister()

        val girlScene = Scene(someGirl.scene)
            .apply {
                this.modelInstance.transform.setToWorld(
                    vec3(0f, 15f, -5f), Vector3.Z, Vector3.Y
                )
            }


        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = girlScene
                sceneManager.addScene(girlScene)
            }
            with<Camera3dFollowComponent>()
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
        sceneManager.skyBox = SceneSkybox(environmentCubemap).alsoRegister()
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}