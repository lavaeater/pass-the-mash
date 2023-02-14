package mash.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import depth.ecs.systems.BulletUpdateSystem
import depth.ecs.systems.DebugRenderSystem3d
import depth.ecs.systems.UpdatePointLightSystem
import eater.core.MainGame
import eater.ecs.ashley.systems.RemoveEntitySystem
import eater.injection.InjectionContext
import ktx.actors.stage
import ktx.ashley.getSystem
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.inject.Context
import ktx.math.vec3
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.label
import mash.core.GameScreen
import mash.ecs.systems.KeepCarFromFlippingSystem
import mash.ecs.systems.RenderSystem3d
import mash.ecs.systems.UpdatePerspectiveCameraSystem
import mash.ecs.systems.VehicleControlSystem
import mash.factories.CarSceneLoader
import mash.factories.TrackGenerator
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
        val skin = Skin("metal-ui/metal-ui.json".toInternalFile())
        Scene2DSkin.defaultSkin = skin

        buildContext {
            val gameSettings = GameSettings()
            bindSingleton(gameSettings)
            bindSingleton(game)
            bindSingleton(PerspectiveCamera().apply {
                fieldOfView = gameSettings.fieldOfView
                near = gameSettings.cameraNear
                far = gameSettings.cameraFar
                position.set(0f, 15f, 25f)
                lookAt(vec3())
            })
            bindSingleton(
                ExtendViewport(
                    gameSettings.gameWidth,
                    gameSettings.gameHeight,
                    inject<PerspectiveCamera>() as Camera
                )
            )
            bindSingleton(createStage())
            bindSingleton(createSceneManager())
            setupBullet(this)
            bindSingleton(getEngine())
            bindSingleton(InputMultiplexer(inject<Engine>().getSystem<VehicleControlSystem>(), inject<Stage>()).apply {
                Gdx.input.inputProcessor = this
            })
            bindSingleton(TrackGenerator())
            bindSingleton(
                GameScreen(
                    CarSceneLoader(inject()),
                    inject(),
                    inject(),
                    inject(),
                    inject()
                )
            )
        }
    }

    private fun createStage(): Stage {
        val vp = ScreenViewport()
        return stage(viewport = vp).apply {
            actors {
                label("WHAWTHWHWTHT").apply {
                    setFontScale(10f)
                    setPosition(vp.worldWidth / 2f, vp.worldHeight / 2f)
                }
            }
        }

    }

    fun createSceneManager(): SceneManager {
        val sceneManager = SceneManager().apply {
            setCamera(inject<PerspectiveCamera>())
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
            addSystem(VehicleControlSystem())
            addSystem(BulletUpdateSystem(inject()))
            //addSystem(KeepCarFromFlippingSystem())
            addSystem(UpdatePerspectiveCameraSystem(inject()))
//            addSystem(CameraControlSystem(inject()))
            addSystem(UpdatePointLightSystem())
            addSystem(RenderSystem3d(inject()))
            addSystem(DebugRenderSystem3d(inject<ExtendViewport>(), inject()))
        }
    }
}
