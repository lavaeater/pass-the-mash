package mash.factories

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

abstract class SceneLoader(): DisposableRegistry by DisposableContainer() {

    protected fun createFloor(
        width: Float,
        height: Float,
        depth: Float,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld
    ) {
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "floor",
            GL20.GL_TRIANGLES,
            (VertexAttribute.Position().usage or VertexAttribute.Normal().usage or VertexAttribute.TexCoords(
                0
            ).usage).toLong(),
            Material()
        )
        BoxShapeBuilder.build(meshBuilder, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width / 2f, height / 2f, depth / 2f))
        val floor = modelBuilder.end()
        val floorInstance = ModelInstance(floor)
        floorInstance.transform.trn(0f, -0.5f, 0f)

        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info)
        body.worldTransform = floorInstance.transform
        sceneManager.addScene(Scene(floorInstance))
        dynamicsWorld.addRigidBody(body)
    }

    abstract fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld)
}

