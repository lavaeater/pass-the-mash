package mash.factories

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.VertexAttributes.Usage.*
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.CF_STATIC_OBJECT
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.assets.toInternalFile
import net.mgsx.gltf.data.material.GLTFMaterial
import net.mgsx.gltf.loaders.shared.geometry.MeshTangentSpaceGenerator
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

object BulletStuffCreator {
    val materials = mutableMapOf<String, Material>()

    fun createMaterial(name: String, textureFileExtension: String = "png", uScale: Float = 1f, vScale:Float = 1f): Material {
        if (!materials.containsKey(name)) {
            val material = Material()

            val diffuseTexture = Texture("textures/$name/${name}_albedo.$textureFileExtension".toInternalFile(), true).apply {
//                setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
            }
            val normalTexture =
                Texture("textures/$name/${name}_normal-ogl.$textureFileExtension".toInternalFile(), true).apply {
//                    setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
                }
            val mrTexture = Texture("textures/$name/${name}_roughness.$textureFileExtension".toInternalFile(), true).apply {
//                setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
            }
            material.set(PBRTextureAttribute.createBaseColorTexture(diffuseTexture).apply {
//                this.rotationUV = 0f
                this.scaleU = uScale
                this.scaleV = vScale
                textureDescription.uWrap = Texture.TextureWrap.Repeat
                textureDescription.vWrap = Texture.TextureWrap.Repeat
            })
//            material.set(PBRTextureAttribute.createNormalTexture(normalTexture).apply {
//                this.rotationUV = 90f
//                textureDescription.uWrap = Texture.TextureWrap.ClampToEdge
//                textureDescription.vWrap = Texture.TextureWrap.ClampToEdge
//            })
//            material.set(PBRTextureAttribute.createMetallicRoughnessTexture(mrTexture).apply {
//                textureDescription.uWrap = Texture.TextureWrap.ClampToEdge
//                textureDescription.vWrap = Texture.TextureWrap.ClampToEdge
//            })
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
        dynamicsWorld: btDynamicsWorld,
        group: Int,
        mask: Int
    ) {
        val widthHeightFactor = width / height
        val material = createMaterial("space-cruiser-panels2", "png", 4f, 4f / widthHeightFactor)
        createBox(
            "wall",
            material,
            width,
            height,
            depth,
            position,
            CF_STATIC_OBJECT,
            sceneManager,
            dynamicsWorld,
            group,
            mask
        )
    }

    fun createTiledFloor(
        width: Float,
        height: Float,
        depth: Float,
        position: Vector3,
        sceneManager: SceneManager,
        dynamicsWorld: btDynamicsWorld,
        group: Int,
        mask: Int
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
            dynamicsWorld,
            group,
            mask
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
        dynamicsWorld: btDynamicsWorld,
        group: Int,
        mask: Int
    ) {
        val mb = ModelBuilder()
        mb.begin()
        val attributes = VertexAttributes(
            VertexAttribute.Position(),
//            VertexAttribute.Normal(),
//            VertexAttribute(Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE),
            VertexAttribute.TexCoords(0)
        )
        val mpb = mb.part(
            id,
            GL20.GL_TRIANGLES,
            attributes,
            material
        )
        BoxShapeBuilder.build(mpb, width, height, depth)
        val btBoxShape = btBoxShape(Vector3(width / 2f, height / 2f, depth / 2f))
        val box = mb.end()

        box.meshes.forEach { mesh ->
//            MeshTangentSpaceGenerator.computeTangentSpace(mesh, material, true, true)
        }
        val boxInstance = ModelInstance(box)
        boxInstance.transform.setToWorld(position, Vector3.Z, Vector3.Y)

        val info = btRigidBody.btRigidBodyConstructionInfo(0f, null, btBoxShape, Vector3.Zero)
        val body = btRigidBody(info).apply {
            collisionFlags = collisionFlags or CF_CUSTOM_MATERIAL_CALLBACK or CF_STATIC_OBJECT
        }
        body.worldTransform = boxInstance.transform
        sceneManager.addScene(Scene(boxInstance))
        dynamicsWorld.addRigidBody(body, group, mask)
        BulletInstances.addInstance(body)
    }

}