///*******************************************************************************
// * Copyright 2011 See AUTHORS file.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package mash.tester.bullet
//
//import com.badlogic.gdx.Gdx
//import com.badlogic.gdx.graphics.*
//import com.badlogic.gdx.graphics.g3d.Model
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.*
//
///** @author xoppa, didum
// */
//class ConvexHullDistanceTest : BaseBulletTest() {
//    private var distance: ConvexHullDistance? = null
//    private var shapeRenderer: ShapeRenderer? = null
//    override fun create() {
//        super.create()
//        val carModel = objLoader.loadModel(Gdx.files.internal("data/car.obj"))
//        disposables.add(carModel)
//        carModel.materials[0].clear()
//        carModel.materials[0][ColorAttribute.createDiffuse(Color.WHITE)] =
//            ColorAttribute.createSpecular(Color.WHITE)
//        world!!.addConstructor("car", BulletConstructor(carModel, 5f, createConvexHullShape(carModel, true)))
//
//        // Create the entities
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        var y = 10f
//        while (y < 50f) {
//            world!!.add("car", -2f + Math.random().toFloat() * 4f, y, -2f + Math.random().toFloat() * 4f)!!
//                .setColor(
//                    0.25f + 0.5f * Math.random().toFloat(),
//                    0.25f + 0.5f * Math.random().toFloat(),
//                    0.25f + 0.5f * Math.random().toFloat(),
//                    1f
//                )
//            y += 5f
//        }
//        distance = ConvexHullDistance()
//        shapeRenderer = ShapeRenderer()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//
//    override fun render() {
//        super.render()
//
//        // Draw the lines of the distances
//        camera!!.update()
//        shapeRenderer!!.projectionMatrix = camera!!.combined
//        shapeRenderer!!.begin(ShapeType.Line)
//        shapeRenderer!!.setColor(1f, 1f, 0f, 1f)
//        for (i in 0 until world!!.entities.size) {
//            val collisionObject0 = world!!.entities[i]!!.body
//            for (j in 0 until world!!.entities.size) {
//                if (i != j) {
//                    val collisionObject1 = world!!.entities[j]!!.body
//                    distance!!.calculateDistance(collisionObject0, collisionObject1)
//                    shapeRenderer.line(distance.getVector3().get(0), distance.getVector3().get(1))
//                }
//            }
//        }
//        shapeRenderer!!.end()
//    }
//
//    private inner class ConvexHullDistance {
//        private val collisionConfiguration: btDefaultCollisionConfiguration
//        private val dispatcher: btCollisionDispatcher
//        private val pairCache: btDbvtBroadphase
//        private val collisionWorld: btCollisionWorld
//        var vector3 = arrayOf(Vector3(), Vector3())
//
//        init {
//            collisionConfiguration = btDefaultCollisionConfiguration()
//            dispatcher = btCollisionDispatcher(collisionConfiguration)
//            pairCache = btDbvtBroadphase()
//            collisionWorld = btCollisionWorld(dispatcher, pairCache, collisionConfiguration)
//        }
//
//        fun calculateDistance(colObjA: btCollisionObject?, colObjB: btCollisionObject?) {
//            val result = DistanceInternalResultCallback()
//            Collision.setGContactBreakingThreshold(100f)
//            collisionWorld.contactPairTest(colObjA, colObjB, result)
//            Collision.setGContactBreakingThreshold(0.02f)
//        }
//
//        private inner class DistanceInternalResultCallback : ContactResultCallback() {
//            override fun addSingleResult(
//                cp: btManifoldPoint, colObj0Wrap: btCollisionObjectWrapper, partId0: Int, index0: Int,
//                colObj1Wrap: btCollisionObjectWrapper, partId1: Int, index1: Int
//            ): Float {
//                cp.getPositionWorldOnA(vector3[0])
//                cp.getPositionWorldOnB(vector3[1])
//                return 1f
//            }
//        }
//    }
//
//    companion object {
//        fun createConvexHullShape(model: Model, optimize: Boolean): btConvexHullShape {
//            val mesh = model.meshes[0]
//            val shape = btConvexHullShape(
//                mesh.getVerticesBuffer(false), mesh.numVertices,
//                mesh.vertexSize
//            )
//            if (!optimize) return shape
//            // now optimize the shape
//            val hull = btShapeHull(shape)
//            hull.buildHull(shape.margin)
//            val result = btConvexHullShape(hull)
//            // delete the temporary shape
//            shape.dispose()
//            hull.dispose()
//            return result
//        }
//    }
//}