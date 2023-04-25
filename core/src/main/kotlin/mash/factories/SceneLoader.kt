package mash.factories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.toInternalFile
import net.mgsx.gltf.loaders.shared.geometry.MeshTangentSpaceGenerator
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

fun createMaterial(name: String) : Material {
    val material = Material()

    val diffuseTexture = Texture("textures/$name/${name}_albedo.png".toInternalFile(), true)
    val normalTexture = Texture("textures/$name/${name}_normal-ogl.png".toInternalFile(), true)
    val mrTexture = Texture("textures/$name/${name}_roughness.png".toInternalFile(), true)
    material.set(PBRTextureAttribute.createBaseColorTexture(diffuseTexture))
    material.set(PBRTextureAttribute.createNormalTexture(normalTexture))
    material.set(PBRTextureAttribute.createMetallicRoughnessTexture(mrTexture))
    return material
}

abstract class SceneLoader : DisposableRegistry by DisposableContainer() {

    protected fun createBrickFloor(
        width: Float,
        height: Float,
        depth: Float,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld
    ) {
        val mb = ModelBuilder()
        mb.begin()
        val material = createMaterial("shades-tile")

        val attributes = VertexAttributes(
            VertexAttribute.Position(),
            VertexAttribute.Normal(),
            VertexAttribute(VertexAttributes.Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE),
            VertexAttribute.TexCoords(0)
        )
        val mpb = mb.part("floor", GL20.GL_TRIANGLES, attributes, material)
        BoxShapeBuilder.build(mpb, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width / 2f, height / 2f, depth / 2f))
        val floor = mb.end()

        floor.meshes.forEach { mesh ->
            MeshTangentSpaceGenerator.computeTangentSpace(mesh, material, false, true)
        }
        val floorInstance = ModelInstance(floor)
        floorInstance.transform.trn(0f, -0.5f, 0f)

        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info)
        body.worldTransform = floorInstance.transform
        sceneManager.addScene(Scene(floorInstance))
        dynamicsWorld.addRigidBody(body)
    }


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

