package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import depth.ecs.components.*
import eater.core.engine
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.disposeSafely
import ktx.math.div
import ktx.math.vec3
import mash.ecs.components.KeyboardControlComponent
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

class CarSceneLoader : SceneLoader() {
    override fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        /**
         * Is it reasonable to just load resources here that we just might only need
         * for this particular scene?
         */
        createFloor(100f, 0f, 100f, sceneManager, dynamicsWorld)
        loadCar(sceneManager, dynamicsWorld)
    }

    fun loadCar(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld) {
        val someCar = GLTFLoader().load(Gdx.files.internal("models/my-cars/model.gltf")).alsoRegister()

        val carScene = Scene(someCar.scene).apply {
            this.modelInstance.transform.setToWorld(
                vec3(0f, 5f, 0f), Vector3.Z, Vector3.Y
            )
        }

        val modelVertexArray = btTriangleIndexVertexArray(carScene.modelInstance.model.meshParts)
        val carShape = btGImpactMeshShape(modelVertexArray)

        //I have to fix this
        carShape.localScaling = Vector3(1f, 1f, 1f)
        carShape.margin = 1f
        val localInertia = vec3()

//        carShape.calculateLocalInertia(1f, localInertia)

//        val boundingBox = BoundingBox()
//        carScene.modelInstance.calculateBoundingBox(boundingBox)
//        val dimensions = vec3(
//            boundingBox.width / 2f,
//            boundingBox.height / 2f,
//            boundingBox.height / 2f
//        )

        engine().entity {
            with<VisibleComponent>()
            with<SceneComponent> {
                scene = carScene
                sceneManager.addScene(carScene)
            }
            lateinit var motionState: MotionState
            with<MotionState> {
                motionState = this
                transform = carScene.modelInstance.transform
            }
            with<Camera3dFollowComponent> {
                distance = 2.5f
                offset.set(5f, 5f, 5f)
            }
            with<BulletRigidBody> {
                carShape.calculateLocalInertia(10f, localInertia)
                carShape.updateBound()
                val info = btRigidBody.btRigidBodyConstructionInfo(10f, motionState, carShape, localInertia)
                val carBody = btRigidBody(info).apply {
                    setDamping(0.5f, 0.5f)
                }
                rigidBody = carBody
                dynamicsWorld.addRigidBody(carBody)
            }
            with<KeyboardControlComponent>()
        }
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}