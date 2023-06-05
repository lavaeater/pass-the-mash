package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape
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
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.model.NodePlus
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil
import threedee.bullet.AttachedNode
import threedee.bullet.MotionState
import threedee.ecs.components.*
import twodee.core.engine
import twodee.ecs.ashley.components.Player

class GirlSceneLoader : SceneLoader() {
    val anims = listOf("idle", "walking-backwards", "lowcrawl", "pistol-walk", "rifle-walk")

    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        setUpScene(sceneManager)
        BulletStuffCreator
            .createTiledFloor(
                25f,
                1f,
                25f,
                vec3(0f, -1f, 0f),
                sceneManager,
                dynamicsWorld,
                CollisionFlags.FLOOR,
                CollisionFlags.ALL
            )
        BulletStuffCreator
            .createWall(
                1f,
                6f,
                24f,
                vec3(5f, 3f, 5f),
                sceneManager,
                dynamicsWorld,
                CollisionFlags.WALL,
                CollisionFlags.ALL
            )
        loadGirlKinematic(sceneManager, dynamicsWorld)
    }

    private fun printNode(node: Node, level: Int = 0) {
        val tabs = (0..level).joinToString("") { " " }
        info { "$tabs${node.id} ${node.isAnimated} ${node.childCount}" }

        if (node.hasChildren()) {
            info { "${tabs}Children: " }
            node.children.forEach {
                printNode(it, level + 1)
            }
        }
    }

    private fun loadGirlKinematic(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val girlAsset = "models/girl-walking.glb".loadModel().alsoRegister()

        val pistolAsset = "models/weapons/pistol.glb".loadModel().alsoRegister()

        val attachedNodes = mutableListOf<AttachedNode>()
//        girlAsset.scene.model.nodes.forEach {
//            if(it.id.contains("Hand")) {
//                attachedNodes.add(AttachedNode(it))
//            }
//            printNode(it)
//        }

        girlAsset.scene.model.getNode("mixamorig:RightHand")?.apply {
            info { "Found right hand" }

            val gun = pistolAsset.scene.model.nodes.get(0).copy()
            gun.detach()

            val gun2 = pistolAsset.scene.model.nodes.get(1).copy()
            gun2.detach()

            gun.addChild(gun2)
            val totalGunNode = NodePlus().apply {
                id = "gun-node"
                addChild(gun)
                scale.set(15f, 15f, 15f)
                translation.set(0f, 0.3f, 0.05f)
                rotation.setEulerAngles(0f, -90f, -90f)
            }
            this.addChild(totalGunNode)
        }



        girlAsset.scene.model.materials.forEach {
            it.set(PBRColorAttribute.createFog(Color.GREEN))
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

        val boundingBox =
            girlScene.modelInstance.calculateBoundingBox(BoundingBox()) //BoundingBox(vec3(0f,0f,0f), vec3(1f,2.5f,1f))
        boundingBox.mul(Matrix4().scl(0.5f, 1f, 1f))
        boundingBox.update()
        val girlShape = btCompoundShape().apply {
            val transform = Matrix4()
            transform.setTranslation(boundingBox.getCenter(vec3()))
            addChildShape(transform, boundingBox.getBoxShape())
        }
        val inertiaVector = vec3()
        val mass = 0f
        girlShape.calculateLocalInertia(mass, inertiaVector)

        val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(mass, null, girlShape, inertiaVector)
        val girlBody = btRigidBody(bodyInfo)
            .apply {
                activationState = Collision.DISABLE_DEACTIVATION
                angularFactor = Vector3.Y
                collisionFlags = collisionFlags or CF_KINEMATIC_OBJECT or CF_CUSTOM_MATERIAL_CALLBACK
            }

        val motionState = MotionState(girlScene.modelInstance.transform)

        dynamicsWorld.addRigidBody(girlBody.apply {
            this.motionState = motionState
        }, CollisionFlags.CHARACTER, CollisionFlags.ALL)

        createCharacterEntityWithKinematicComponent(girlScene, sceneManager, girlBody, attachedNodes)
    }

    private fun createCharacterEntityWithKinematicComponent(
        characterScene: Scene,
        sceneManager: SceneManager,
        characterBody: btRigidBody,
        attachedNodes: MutableList<AttachedNode>
    ) {
        BulletInstances.addEntity(characterBody,
            engine().entity {
                with<VisibleComponent>()
                with<SceneComponent> {
                    scene = characterScene
                    sceneManager.addScene(characterScene)
                }
                with<SpotLightComponent> {
                    spotLightEx.setConeDeg(45f, 15f)
                    spotLightEx.range = 5f
                    spotLightEx.intensity = 100f
                    spotLightEx.setColor(Color.YELLOW)
                    offset.set(0f, 50f, 0.5f)
                    sceneManager.environment.add(spotLightEx)
                }
                with<CharacterAnimationComponent> {
                    characterAnimationState = CharacterAnimationState(characterScene.animationController)
                }
                with<KinematicObject> {
                    kinematicBody = characterBody
                }
                with<MotionStateComponent> {
                    motionState = characterBody.motionState as MotionState
                }
                with<Player>()
                with<AttachedNodesComponent> {
                    this.attachedNodes.addAll(attachedNodes)
                }
                with<IsometricCameraFollowComponent>()
                with<CharacterControlComponent>()
            })
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

        sceneManager.setAmbientLight(0.5f)
        sceneManager.environment.set(PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT))
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))
//        sceneManager.environment.apply {
//            add(DirectionalShadowLight().apply {
//                set(0f, 1f, 0f, -1f, -1f, 0f)
//            }.alsoRegister())
//        }
        // setup skybox
        sceneManager.skyBox = SceneSkybox(environmentCubemap).alsoRegister()
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}