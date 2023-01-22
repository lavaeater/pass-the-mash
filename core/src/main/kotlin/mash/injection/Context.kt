package mash.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld
import com.badlogic.gdx.utils.viewport.ExtendViewport
import depth.ecs.systems.*
import eater.core.MainGame
import eater.ecs.ashley.systems.RemoveEntitySystem
import eater.injection.InjectionContext
import ktx.assets.disposeSafely
import ktx.inject.Context
import ktx.math.vec3
import mash.core.GameScreen
import mash.factories.CarSceneLoader
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.SceneManager


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
            bindSingleton(game)
            bindSingleton(PerspectiveCamera().apply {
                fieldOfView = 67f
                near = 1f
                far = 3000f
                position.set(5f, 5f, 5f)
                lookAt(vec3())
            })
            bindSingleton(
                ExtendViewport(
                    gameSettings.gameWidth,
                    gameSettings.gameHeight,
                    inject<PerspectiveCamera>() as Camera
                )
            )
            bindSingleton(createSceneManager())
            setupBullet(this)
            bindSingleton(getEngine())
            bindSingleton(
                GameScreen(
                CarSceneLoader(),
                inject(),
                inject(),
                inject()
            )
            )
        }
    }

    fun createSceneManager(): SceneManager {
        val sceneManager = SceneManager().apply {
            setCamera(inject<PerspectiveCamera>())
        }
        sceneManager.environment.apply {

            set(ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, .1f, 1f))
            add(DirectionalShadowLight().apply {
                set(1f, 0.5f, 1f, -1f, -1f, 0f)
            })
            add(DirectionalShadowLight().apply {
                set(0.5f, 1f, 1f, 1f, -1f, -1f)
            })
        }
//        // setup skybox
//        val skybox = SceneSkybox(environmentCubemap)
//        sceneManager.skyBox = skybox
        return sceneManager
    }

    private fun setupBullet(context: Context) {
        context.apply {
            bindSingleton<btCollisionConfiguration>(btDefaultCollisionConfiguration())
            bindSingleton<btDispatcher>(btCollisionDispatcher(inject()))
            bindSingleton<btBroadphaseInterface>(btDbvtBroadphase())
            bindSingleton<btConstraintSolver>(btSequentialImpulseConstraintSolver())
            bindSingleton<btDynamicsWorld>(btSoftRigidDynamicsWorld(inject(), inject(), inject(), inject()).apply {
                gravity = vec3(0f, -10f, 0f)
            })

        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(RemoveEntitySystem())
            addSystem(UpdatePointLightSystem())
            addSystem(UpdatePerspectiveCameraSystem(inject()))
            addSystem(BulletUpdateSystem(inject()))
            addSystem(RenderSystem3d(inject()))
//            addSystem(DebugRenderSystem3d(inject<ExtendViewport>(), inject()))
            addSystem(EntityControlSystem())
        }
    }
}
