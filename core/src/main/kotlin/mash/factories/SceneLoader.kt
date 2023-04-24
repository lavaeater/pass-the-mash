package mash.factories

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
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
import ktx.assets.toInternalFile
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

abstract class SceneLoader : DisposableRegistry by DisposableContainer() {

    protected fun createFloor(
        width: Float,
        height: Float,
        depth: Float,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld
    ) {
        val checkboard = Texture("data/g3d/checkboard.png".toInternalFile())
        val material = Material().apply {
            set(PBRTextureAttribute.createBaseColorTexture(checkboard))
            set(PBRColorAttribute.createSpecular(Color.WHITE))
            set(PBRFloatAttribute.createShininess(16f))
        }
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val meshBuilder = modelBuilder.part(
            "floor",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong(),
            material
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
    protected fun setUpScene(sceneManager: SceneManager) {
        setupEnvironment(sceneManager)
    }

    abstract fun setupEnvironment(sceneManager: SceneManager)
}

