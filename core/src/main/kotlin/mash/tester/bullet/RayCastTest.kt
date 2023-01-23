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
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback
//import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
//
///** @author xoppa
// */
//class RayCastTest : BaseBulletTest() {
//    val BOXCOUNT_X = 5
//    val BOXCOUNT_Y = 5
//    val BOXCOUNT_Z = 1
//    val BOXOFFSET_X = 0f
//    val BOXOFFSET_Y = 0.5f
//    val BOXOFFSET_Z = 2.5f
//    var rayTestCB: ClosestRayResultCallback? = null
//    var rayFrom = Vector3()
//    var rayTo = Vector3()
//    override fun create() {
//        super.create()
//        instructions =
//            "Tap a box to ray cast\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//
//        // Create the entities
//        world!!.add("ground", -7f, 0f, -7f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        for (x in 0 until BOXCOUNT_X) {
//            for (y in 0 until BOXCOUNT_Y) {
//                for (z in 0 until BOXCOUNT_Z) {
//                    world!!.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)!!
//                        .setColor(
//                            0.5f + 0.5f * Math.random().toFloat(),
//                            0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(), 1f
//                        )
//                }
//            }
//        }
//        rayTestCB = ClosestRayResultCallback(Vector3.Zero, Vector3.Z)
//    }
//
//    override fun dispose() {
//        if (rayTestCB != null) rayTestCB!!.dispose()
//        rayTestCB = null
//        super.dispose()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        val ray = camera!!.getPickRay(x, y)
//        rayFrom.set(ray.origin)
//        rayTo.set(ray.direction).scl(50f).add(rayFrom) // 50 meters max from the origin
//
//        // Because we reuse the ClosestRayResultCallback, we need reset it's values
//        rayTestCB!!.collisionObject = null
//        rayTestCB!!.closestHitFraction = 1f
//        rayTestCB!!.setRayFromWorld(rayFrom)
//        rayTestCB!!.setRayToWorld(rayTo)
//        world!!.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB)
//        if (rayTestCB!!.hasHit()) {
//            val obj = rayTestCB!!.collisionObject
//            if (!obj.isStaticOrKinematicObject) {
//                val body = obj as btRigidBody
//                body.activate()
//                body.applyCentralImpulse(BaseBulletTest.Companion.tmpV2.set(ray.direction).scl(20f))
//            }
//        }
//        return true
//    }
//}