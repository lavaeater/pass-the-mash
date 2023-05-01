package mash.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.vfx.VfxManager
import com.crashinvaders.vfx.effects.CrtEffect
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.inject.Context
import ktx.math.vec3
import mash.core.GameScreen
import mash.ecs.systems.BulletGhostObjectControlSystem
import mash.factories.GirlSceneLoader
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import threedee.ecs.systems.*
import twodee.core.MainGame
import twodee.ecs.ashley.systems.RemoveEntitySystem
import twodee.injection.InjectionContext


object Context : InjectionContext() {
    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    fun initialize(game: MainGame) {
        buildContext {
            val gameSettings = GameSettings()
            bindSingleton(gameSettings)
            bindSingleton(VfxManager(Pixmap.Format.RGBA8888).apply {
                addEffect(CrtEffect())
            })
            bindSingleton(game)
            bindSingleton(PerspectiveCamera().apply {
                fieldOfView = gameSettings.fieldOfView
                near = gameSettings.cameraNear
                far = gameSettings.cameraFar
                position.set(10f, 10f, 10f)
                lookAt(vec3())
            })
            bindSingleton(OrthographicCamera().apply {
                setToOrtho(false)
                position.set(1f, 1f, 1f)
                near = -25f
                far = 100f
                rotate(Vector3.X, -30f)
                rotate(Vector3.Y, -45f)
            })
            bindSingleton(
                ExtendViewport(
                    gameSettings.gameWidth,
                    gameSettings.gameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(createSceneManager())
            setupBullet(this)
            bindSingleton(getEngine())
            //            bindSingleton(TrackGenerator())
            bindSingleton(
                GameScreen(
                    GirlSceneLoader(),
                    inject(),
                    inject(),
                    inject(),
                    inject()
                )
            )
        }
    }

    private fun createSceneManager(): SceneManager {

        val config = PBRShaderProvider.createDefaultConfig()
        config.numBones = 60
        config.numDirectionalLights = 1
        config.numPointLights = 5

        config.fragmentShader = "shaders/default/gdx-pbr.fs.glsl".toInternalFile().readString()
        config.vertexShader = "shaders/default/gdx-pbr.vs.glsl".toInternalFile().readString()


        val depthConfig = PBRShaderProvider.createDefaultDepthConfig()
        depthConfig.numBones = 60

        val sceneManager = SceneManager(PBRShaderProvider(config), PBRDepthShaderProvider(depthConfig)).apply {
            setCamera(inject<OrthographicCamera>())
        }
        sceneManager.environment.apply {
            return sceneManager
        }
    }

    private fun setupBullet(context: Context) {
        context.apply {
            bindSingleton<btCollisionConfiguration>(btDefaultCollisionConfiguration())
            bindSingleton<btDispatcher>(btCollisionDispatcher(inject()))
            bindSingleton<btBroadphaseInterface>(btDbvtBroadphase())
            bindSingleton<btConstraintSolver>(btSequentialImpulseConstraintSolver())
            bindSingleton<btDynamicsWorld>(
                btSoftRigidDynamicsWorld(
                    inject(),
                    inject(),
                    inject(),
                    inject()
                ).apply {
                    gravity = vec3(0f, -10f, 0f)
                })

        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(RemoveEntitySystem())
            addSystem(BulletUpdateSystem(inject()))
            addSystem(BulletGhostObjectControlSystem())
            addSystem(UpdateOrthographicCameraSystem(inject()))
            addSystem(UpdatePointLightSystem())
            addSystem(Animation3dSystem())
            addSystem(
                RenderSystem3d(
                    inject(),
                )
            )
//            addSystem(DebugRenderSystem3d(inject<ExtendViewport>(), inject()))
        }
    }
}
