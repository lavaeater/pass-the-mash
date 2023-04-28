package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape
import com.badlogic.gdx.physics.bullet.collision.btGhostObject
import com.badlogic.gdx.physics.bullet.collision.btShapeHull
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.collections.toGdxArray
import ktx.log.info
import ktx.math.vec3
import mash.core.getBoxShape
import mash.core.loadModel
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil
import threedee.bullet.MotionState
import threedee.ecs.components.*
import twodee.core.engine
import twodee.ecs.ashley.components.Player


class GirlSceneLoader : SceneLoader() {
    val anims = listOf("idle", "walking-backwards", "lowcrawl", "pistol-walk", "rifle-walk")

    fun createConvexHullShape(model: Model, optimize: Boolean): btConvexHullShape {
        val mesh = model.meshes[0]
        val shape = btConvexHullShape(
            mesh.verticesBuffer, mesh.numVertices,
            mesh.vertexSize
        )
        if (!optimize) return shape
        // now optimize the shape
        val hull = btShapeHull(shape)
        hull.buildHull(shape.margin)
        val result = btConvexHullShape(hull)
        // delete the temporary shape
        shape.dispose()
        hull.dispose()
        return result
    }

    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        setUpScene(sceneManager)
        BulletStuffCreator.createTiledFloor(25f, 1f, 25f, vec3(0f, -1f, 0f), sceneManager, dynamicsWorld)
        BulletStuffCreator.createWall(1f, 5f, 25f, vec3(5f, 2f, 5f), sceneManager, dynamicsWorld)
        loadGirl(sceneManager, dynamicsWorld)
    }

    private fun printNode(node: Node, level: Int = 0) {
        val tabs = (0..level).joinToString("") { " " }
        info { "$tabs${node.id} ${node.isAnimated} ${node.childCount}"  }

        if(node.hasChildren()) {
            info { "${tabs}Children: " }
            node.children.forEach {
                printNode(it, level + 1)
            }
        }
    }

    private fun loadGirl(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val girlAsset = "models/girl-walking.glb".loadModel().alsoRegister()

        girlAsset.scene.model.nodes.forEach {
            printNode(it)
        }

        girlAsset.animations.first().id = "walking"

        val loadedAnims = anims.map {
            val m = "models/girl-$it.glb".loadModel()

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
                    vec3(0f, 0f, 0f), Vector3.Z, Vector3.Y
                )
            }

        val boundingBox = girlScene.modelInstance.calculateBoundingBox(BoundingBox()) //BoundingBox(vec3(0f,0f,0f), vec3(1f,2.5f,1f))
        boundingBox.mul(girlScene.modelInstance.transform)
        boundingBox.update()
        val girlShape = btCompoundShape().apply {
            val transform = Matrix4()
            transform.setTranslation(boundingBox.getCenter(vec3()))
            addChildShape(transform, boundingBox.getBoxShape())
        }
//        val localInertia = vec3()
//        val mass = 1f
//        girlShape.calculateLocalInertia(mass, localInertia)
        val ms = MotionState(girlScene.modelInstance.transform)
//
//        val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(mass, null, girlShape, localInertia)
        val girlBody = btGhostObject().apply {
            collisionShape = girlShape
        }

        dynamicsWorld.addCollisionObject(girlBody)


//            btRigidBody(bodyInfo).apply {
//        }.alsoRegister()
//        dynamicsWorld.addRigidBody(girlBody)

        createCharacterEntity(girlScene, sceneManager, girlBody, ms)
    }

    private fun createCharacterEntity(
        characterScene: Scene,
        sceneManager: SceneManager,
        characterBody: btGhostObject,
        motionState: MotionState
    ) {
        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = characterScene
                sceneManager.addScene(characterScene)
            }
            with<CharacterAnimationStateComponent> {
                stateMachine = CharacterStateMachine(characterScene.animationController)
            }
            with<BulletGhostObject> {
                ghostObject = characterBody
            }
            with<Player>()
            with<IsometricCameraFollowComponent>()
            with<KeyboardControlComponent>()
            with<MotionStateComponent> {
                this.motionState = motionState
            }
        }
    }

    private fun createCharacterEntity(
        characterScene: Scene,
        sceneManager: SceneManager,
        characterBody: btRigidBody,
        motionState: MotionState
    ) {
        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = characterScene
                sceneManager.addScene(characterScene)
            }
            with<CharacterAnimationStateComponent> {
                stateMachine = CharacterStateMachine(characterScene.animationController)
//                animations = characterAsset.animations
//                animationController = characterScene.animationController
//                animationController.setAnimation("walking", -1, 0.75f, null)
            }
            with<BulletRigidBody> {
                rigidBody = characterBody
            }
            with<Player>()
            with<IsometricCameraFollowComponent>()
            with<KeyboardControlComponent>()
            with<MotionStateComponent> {
                this.motionState = motionState
                characterBody.motionState = motionState
            }
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