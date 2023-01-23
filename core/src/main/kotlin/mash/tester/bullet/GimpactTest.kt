package mash.tester.bullet

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btGImpactCollisionAlgorithm
import com.badlogic.gdx.physics.bullet.collision.btGImpactMeshShape
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray

/** @author Xoppa
 */
class GimpactTest : BaseBulletTest() {
    var ground: BulletEntity? = null
    var chassisVertexArray: btTriangleIndexVertexArray? = null
    override fun create() {
        super.create()
        val chassisModel = objLoader.loadModel(Gdx.files.internal("data/car.obj"))
        disposables.add(chassisModel)
        chassisModel.materials[0].clear()
        chassisModel.materials[0][ColorAttribute.createDiffuse(Color.RED)] =
            ColorAttribute.createSpecular(Color.WHITE)
        chassisVertexArray = btTriangleIndexVertexArray(chassisModel.meshParts)
        val chassisShape = btGImpactMeshShape(chassisVertexArray)
        chassisShape.localScaling = Vector3(1f, 1f, 1f)
        chassisShape.margin = 0f
        chassisShape.updateBound()
        world!!.addConstructor("chassis", BulletConstructor(chassisModel, 1f, chassisShape))
        world!!.add("ground", 0f, 0f, 0f).also { ground = it }!!.setColor(
            0.25f + 0.5f * Math.random().toFloat(),
            0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 1f
        )
        var y = 10f
        while (y < 50f) {
            world!!.add(
                "chassis",
                -2f + Math.random().toFloat() * 4f,
                y,
                -2f + Math.random().toFloat() * 4f
            )!!.setColor(
                0.25f + 0.5f * Math.random().toFloat(),
                0.25f + 0.5f * Math.random().toFloat(),
                0.25f + 0.5f * Math.random().toFloat(),
                1f
            )
            y += 5f
        }
        btGImpactCollisionAlgorithm.registerAlgorithm(world!!.dispatcher)
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }

    override fun dispose() {
        super.dispose()
        chassisVertexArray!!.dispose()
        chassisVertexArray = null
        ground = null
    }
}