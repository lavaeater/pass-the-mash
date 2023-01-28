package mash.factories

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.math.minus
import ktx.math.random
import ktx.math.unaryMinus
import ktx.math.vec3
import net.mgsx.gltf.scene3d.scene.Scene

class TrackGenerator {
    fun generateTrack(): MashTrack {

        /*

        How does one generate a track? We want the tracks to be self-connective.

        Imagine a travelling point, if you will. This point will have a position in
        3D space, x,y and z.

        It travels "forward", which is some direction in the x-z plane. The direction could be decided
        randomly, to start with.

        We can also define that this travelling point, as it travels forward, will alter its
        elevation in the y-plane, according to either some randomness or some other algorithm.

        At each point this traveller visits, we shall define a left and right point related to it.

        This is the width of the track at that particular place in space. To make things "easy"
        we can define that they are always perpendicular in relation to "forward" of the travelling
        point.
         */

        val centerPoints = mutableListOf<Vector3>()
        val leftPoints = mutableListOf<Vector3>()
        val rightPoints = mutableListOf<Vector3>()

        val travelingPoint = vec3()
        val forward = -Vector3.Z
        val tmpVector = vec3()
        val left = fun(): Vector3 {
            return tmpVector.set(forward).rotate(Vector3.Y, 90f)
        }
        val leftPoint = fun(): Vector3 {
            return travelingPoint.cpy().add(left().scl(5f))
        }

        val right = fun(): Vector3 {
            return tmpVector.set(forward).rotate(Vector3.Y, -90f)
        }
        val rightPoint = fun(): Vector3 {
            return travelingPoint.cpy().add(left().scl(5f))
        }

        for (i in 0..10) {
            centerPoints.add(travelingPoint.cpy())
            leftPoints.add(leftPoint())
            rightPoints.add(rightPoint())

            forward.rotate(Vector3.Y, (-5f..5f).random())
            travelingPoint.add(tmpVector.set(forward).scl(15f))
            travelingPoint.y += (-2.5f..2.5f).random()
        }

        val minY = centerPoints.minOf { it.y }

        // Generate mesh magic goes here!

        /**
         * It is, to start, just a plane with triangles. It's easy, we
         * can probably just use a mesh-builder to do triangles triangles
         * or even rectangles!
         */
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val bbb = modelBuilder.part(
            "TRAAACK",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
            Material()
        )
        val normal = vec3()

        for (i in leftPoints.indices) {
            if (i != leftPoints.lastIndex) {
                val c00 = leftPoints[i]
                val c10 = leftPoints[i + 1]
                val c11 = rightPoints[i + 1]
                val c01 = rightPoints[i]
                val u = c10 - c00
                val v = c11 - c00
                normal.apply {
                    x = -(u.y * v.z - u.z * v.y)
                    y = -(u.z * v.x - u.x * v.z)
                    z = -(u.x * v.y - u.y * v.x)
                }.nor().scl(-1f)
                bbb.rect(c00, c10, c11, c01, Vector3.Y.cpy())
            }
        }
        
        val model = modelBuilder.end()

        var scene = Scene(model).
            apply {
                modelInstance.transform.setToWorld(vec3(),-Vector3.Z, Vector3.Y)
            }

        return MashTrack(scene)
    }


}

class MashTrack(val scene: Scene, val body: btRigidBody? = null) {
}
