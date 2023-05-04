package mash.factories

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.CF_STATIC_OBJECT
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.kotcrab.vis.ui.widget.HorizontalCollapsibleWidget
import ktx.assets.toInternalFile
import ktx.math.amid
import net.mgsx.gltf.loaders.shared.geometry.MeshTangentSpaceGenerator
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

object BulletStuffCreator {
    val materials = mutableMapOf<String, Material>()

    fun createMaterial(name: String, textureFileExtension: String = "png") : Material {
        if (!materials.containsKey(name)) {
            val material = Material()

            val diffuseTexture = Texture("textures/$name/${name}_albedo.$textureFileExtension".toInternalFile(), true)
            val normalTexture = Texture("textures/$name/${name}_normal-ogl.$textureFileExtension".toInternalFile(), true)
            val mrTexture = Texture("textures/$name/${name}_roughness.$textureFileExtension".toInternalFile(), true)
            material.set(PBRTextureAttribute.createBaseColorTexture(diffuseTexture))
            material.set(PBRTextureAttribute.createNormalTexture(normalTexture))
            material.set(PBRTextureAttribute.createMetallicRoughnessTexture(mrTexture))
            materials[name] = material
        }
        return materials[name]!!
    }

    fun createWall(
        width: Float,
        height: Float,
        depth: Float,
        position: Vector3,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld
    ) {
        val material = createMaterial("red-bricks", "jpg")
        createBox(
            "wall",
            material,
            width,
            height,
            depth,
            position,
            CF_STATIC_OBJECT,
            sceneManager,
            dynamicsWorld
        )
    }

    fun createTiledFloor(
        width: Float,
        height: Float,
        depth: Float,
        position: Vector3,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld
    ) {

        val material = createMaterial("shades-tile")
        createBox(
            "floor",
            material,
            width,
            height,
            depth,
            position,
            CF_STATIC_OBJECT,
            sceneManager,
            dynamicsWorld
        )
    }

    fun createBox(
        id: String,
        material: Material,
        width: Float,
        height: Float,
        depth: Float,
        position: Vector3,
        cFlags: Int,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld) {
        val mb = ModelBuilder()
        mb.begin()
        val attributes = VertexAttributes(
            VertexAttribute.Position(),
            VertexAttribute.Normal(),
            VertexAttribute(VertexAttributes.Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE),
            VertexAttribute.TexCoords(0)
        )
        val mpb = mb.part(id, GL20.GL_TRIANGLES, attributes, material)
        BoxShapeBuilder.build(mpb, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width / 2f, height / 2f, depth / 2f))
        val floor = mb.end()

        floor.meshes.forEach { mesh ->
            MeshTangentSpaceGenerator.computeTangentSpace(mesh, material, false, true)
        }
        val floorInstance = ModelInstance(floor)
        floorInstance.transform.setToWorld(position, Vector3.Z, Vector3.Y)

        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info).apply {
            collisionFlags = collisionFlags or cFlags
        }
        body.worldTransform = floorInstance.transform
        sceneManager.addScene(Scene(floorInstance))
        dynamicsWorld.addRigidBody(body)
    }

}