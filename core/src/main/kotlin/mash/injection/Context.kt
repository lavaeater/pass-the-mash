package mash.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.vfx.VfxManager
import com.crashinvaders.vfx.effects.CrtEffect
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.inject.Context
import ktx.math.vec3
import ktx.scene2d.Scene2DSkin
import mash.core.GameScreen
import mash.ecs.systems.DebugRenderSystem3d
import mash.ecs.systems.KinematicObjectControlSystem
import threedee.ecs.systems.UpdateAttachedNodesSystem
import mash.factories.GirlSceneLoader
import mash.shaders.CustomShaderProvider
import mash.ui.ToolHud
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import threedee.ecs.components.CharacterControlComponent
import threedee.ecs.components.KinematicObject
import threedee.ecs.systems.*
import twodee.core.MainGame
import twodee.ecs.ashley.systems.RemoveEntitySystem
import twodee.injection.InjectionContext
import twodee.injection.InjectionContext.Companion.inject

class MyContactListener : ContactListener() {

    override fun onContactAdded(
        cp: btManifoldPoint,
        colObj0: btCollisionObject,
        partId0: Int,
        index0: Int,
        colObj1: btCollisionObject,
        partId1: Int,
        index1: Int
    ): Boolean {
        val someTempVector = vec3()
        return if (colObj0.userData != null || colObj1.userData != null) {

            val userIndex0 = index0
            val userIndex1 = index1

            val entity0 = colObj0.userData as Entity?
            val entity1 = colObj1.userData as Entity?

            if(entity0 != null) {
                if(CharacterControlComponent.has(entity0)) {
                    val ccc = CharacterControlComponent.get(entity0)
                    ccc.clearDirections()
                }

                val kBody = KinematicObject.get(entity0)
                cp.getNormalWorldOnB(someTempVector)

                // If the contact normal is pointing in the opposite direction of the kinematic body's velocity, stop the body
                if (someTempVector.dot(kBody.kinematicBody.linearVelocity) < 0f) {
                    kBody.kinematicBody.linearVelocity = Vector3.Zero
                }
            }

            if(entity1 != null) {
                val kBody = KinematicObject.get(entity1)
                cp.getNormalWorldOnB(someTempVector)

                // If the contact normal is pointing in the opposite direction of the kinematic body's velocity, stop the body
                if (someTempVector.dot(kBody.kinematicBody.linearVelocity) < 0f) {
                    kBody.kinematicBody.linearVelocity = Vector3.Zero
                }
            }


            true
        } else {
            false
        }
    }

    companion object {
        val instance by lazy { inject<MyContactListener>() }
    }
}

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
        Scene2DSkin.defaultSkin = Skin("ui/uiskin.json".toInternalFile())
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
            bindSingleton(InputMultiplexer().apply {
                Gdx.input.inputProcessor = this
            })
            bindSingleton(getEngine())
            //            bindSingleton(TrackGenerator())
            bindSingleton(PolygonSpriteBatch())
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
        config.numSpotLights = 4

        val depthConfig = PBRShaderProvider.createDefaultDepthConfig()
        depthConfig.numBones = 60
        depthConfig.numSpotLights = 4

        val sceneManager =
            SceneManager(
                PBRShaderProvider(config),
                PBRDepthShaderProvider(depthConfig)
            ).apply {
                setCamera(inject<OrthographicCamera>())
            }

        sceneManager.environment.apply {
            return sceneManager
        }
    }

    private fun setupBullet(context: Context) {
        Bullet.init()
        context.apply {
            bindSingleton<btCollisionConfiguration>(btDefaultCollisionConfiguration())
            bindSingleton<btDispatcher>(btCollisionDispatcher(inject()))
            bindSingleton<btBroadphaseInterface>(btDbvtBroadphase())
            bindSingleton<btConstraintSolver>(btSequentialImpulseConstraintSolver())
            bindSingleton<btDynamicsWorld>(
                btDiscreteDynamicsWorld(
                    inject(),
                    inject(),
                    inject(),
                    inject()
                ).apply {
                    gravity = vec3(0f, -10f, 0f)
                })
            bindSingleton(MyContactListener())

        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(RemoveEntitySystem())
            addSystem(BulletUpdateSystem(inject()))
            addSystem(KinematicObjectControlSystem(inject()))
            addSystem(UpdateOrthographicCameraSystem(inject()))
            addSystem(UpdatePointLightSystem())
            addSystem(UpdateSpotLightSystem())
            addSystem(Animation3dSystem())
            addSystem(UpdateAttachedNodesSystem())
            addSystem(
                RenderSystem3d(
                    inject(),
                )
            )
            addSystem(DebugRenderSystem3d(inject<ExtendViewport>(), inject()))
        }
    }
}
