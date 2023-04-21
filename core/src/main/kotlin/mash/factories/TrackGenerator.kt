package mash.factories

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import ktx.assets.toInternalFile
import ktx.math.random
import ktx.math.unaryMinus
import ktx.math.vec3
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
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

        val roadWidth = 50f

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
            return travelingPoint.cpy().add(left().scl(roadWidth))
        }

        val right = fun(): Vector3 {
            return tmpVector.set(forward).rotate(Vector3.Y, -90f)
        }
        val rightPoint = fun(): Vector3 {
            return travelingPoint.cpy().add(right().scl(roadWidth))
        }

        for (i in 0..50) {
            centerPoints.add(travelingPoint.cpy())
            leftPoints.add(leftPoint())
            rightPoints.add(rightPoint())
            forward.rotate(Vector3.Y, (-15f..15f).random())
            travelingPoint.add(tmpVector.set(forward).scl(25f))
            //travelingPoint.y += (-5f..5f).random()
            tmpVector.setZero()
        }

        val minY = centerPoints.minOf { it.y } - 1f
        // Generate mesh magic goes here!
        val checkboard = Texture("data/g3d/checkboard.png".toInternalFile())
        val material = Material().apply {
            set(PBRTextureAttribute.createBaseColorTexture(checkboard))
            set(PBRColorAttribute.createSpecular(Color.WHITE))
            set(PBRFloatAttribute.createShininess(16f))
        }
        /**
         * It is, to start, just a plane with triangles. It's easy, we
         * can probably just use a mesh-builder to do triangles triangles
         * or even rectangles!
         *
         *
         * Oh nooo, we have to do a mesh again, don't we?
         *
         * Well, it shouldn't be that hard, should it? Because the only reason I made boxes with
         * heights was so that I could be 100% that I would actually SEE the models for the ground.
         *
         * So can we do it some other way?
         *
         * And also, perhaps the car could be a different shapen instead of that simple box?
         *
         * Could we use a fucking heightmap for this? Maybe.
         *
         * But go with mesh first, fucker.
         *
         * And just create a plane.
         *
         * An reuse the vertices. But how do we actually do that? We need to keep track of the winding. And
         * the indexes, but I think the index thing isn't that hard, really.
         *
         * a______________b
         * |
         * |
         * |
         * |
         * |
         * |
         * d______________c
         *
         * Lets NOT share the vertices at first. I think the issue is actually the fact that the
         * body of the car hits the floor due to the position of the wheels or some such.
         *
         * And then we of course have the complex stuff of lowering the center of gravity.
         *
         * Try some of that stuff out first. Should we make a UI to set settings for the car?
         *
         * Yes. Yes we should.
         *
         * So, let's say the track generator is DONE for now - we can easily make a mesh out of it later.
         *
         */
        val modelBuilder = ModelBuilder()
        modelBuilder.begin()
        val bbb = modelBuilder.part(
            "TRAAACK",
            GL20.GL_TRIANGLES,
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong(),
            material
        )

        for (i in leftPoints.indices) {
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

        val body = btRigidBody(btRigidBody.btRigidBodyConstructionInfo(0f, null, Bullet.obtainStaticNodeShape(scene.modelInstance.nodes).apply { calculateLocalInertia(0f, Vector3.Zero) }))

        return MashTrack(scene, body)
    }


}

class MashTrack(val scene: Scene, val body: btRigidBody) {
}
