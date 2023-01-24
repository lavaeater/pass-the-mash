package mash.core

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import ktx.assets.toInternalFile
import ktx.math.vec3
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset


fun <E> MutableSet<E>.addIndexed(element: E): Int {
    this.add(element)
    return this.indexOf(element)
}

fun String.loadModel(): SceneAsset {
    return if(this.endsWith(".gltf"))
        GLTFLoader().load(this.toInternalFile())
    else if(this.endsWith(".glb"))
        GLBLoader().load(this.toInternalFile())
    else
        throw Exception("File doesn't seem to be either gltf or glb.")
}

fun BoundingBox.getBoxShape(): btBoxShape {
    return btBoxShape(
        this.getDimensions(vec3()).scl(0.25f)
    )
}

fun Model.calculateBoundingBox(): BoundingBox {
    return this.calculateBoundingBox(BoundingBox())
}

fun Scene.getBoundingBox(): BoundingBox {
    return this.modelInstance.model.calculateBoundingBox()
}

