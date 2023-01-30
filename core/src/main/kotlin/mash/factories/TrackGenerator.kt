package mash.factories

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import depth.ecs.components.MotionState
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
            return travelingPoint.cpy().add(left().scl(15f))
        }

        val right = fun(): Vector3 {
            return tmpVector.set(forward).rotate(Vector3.Y, -90f)
        }
        val rightPoint = fun(): Vector3 {
            return travelingPoint.cpy().add(right().scl(15f))
        }

        for (i in 0..15) {
            centerPoints.add(travelingPoint.cpy())
            leftPoints.add(leftPoint())
            rightPoints.add(rightPoint())
            forward.rotate(Vector3.Y, (-15f..15f).random())
            travelingPoint.add(tmpVector.set(forward).scl(15f))
            travelingPoint.y += (-5f..5f).random()
            tmpVector.setZero()
        }

        val minY = centerPoints.minOf { it.y } - 1f
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
        val normal = vec3(0f, 1f, 0f)
//        val builder = BoxShapeBuilder()
//        val model = modelBuilder.createBox(
//            5f,
//            5f,
//            5f,
//            GL20.GL_TRIANGLES,
//            Material(),
//            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )


        for (i in leftPoints.indices) {

//            if( i > 0)
//                bbb.setVertexTransform(Matrix4().setToWorld(centerPoints[i - 1].cpy(), -Vector3.Z, Vector3.Y))

            if (i != leftPoints.lastIndex) {
                bbb.setColor(Color.RED)

                val c010 = rightPoints[i] // vec3(0f,10f,0f) // near top right
                val c110 = leftPoints[i] // vec3(10f,10f,0f) // near top left
                val c011 = rightPoints[i + 1] //vec3(0f,10f,10f) // far top right
                val c111 = leftPoints[i + 1] // vec3(10f,10f,10f) // far top left

                val c000 = c010.cpy().apply { y = minY } // vec3(0f,0f,0f) // near bottom right
                val c100 = c110.cpy().apply { y = minY } // vec3(10f,0f,0f) // near bottom left
                val c001 = c011.cpy().apply { y = minY } // vec3(0f,0f,10f) // far bottom right
                val c101 = c111.cpy().apply { y = minY } // vec3(10f,0f,10f) // far bottom left


                BoxShapeBuilder.build(
                    bbb,
                    c000,
                    c010,
                    c100,
                    c110,
                    c001,
                    c011,
                    c101,
                    c111
                )
            }
        }

        val model = modelBuilder.end()

        val scene = Scene(model)
//            .apply {
//                this.modelInstance.transform.setToWorld(
//                    vec3(0f, 0f, 0f), Vector3.Z, Vector3.Y
//                )
//            }

        val bodies = scene.modelInstance.nodes.map {
            val rigidBodyShape = Bullet.obtainStaticNodeShape(it, false)
//            val motionState = MotionState(it.globalTransform)
            btRigidBody(btRigidBody.btRigidBodyConstructionInfo(0f, null, rigidBodyShape))
        }



        return MashTrack(scene, bodies)
    }


}

class MashTrack(val scene: Scene, val bodies: List<btRigidBody>) {
}
