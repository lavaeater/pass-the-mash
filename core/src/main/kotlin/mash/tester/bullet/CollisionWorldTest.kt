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
//import com.badlogic.gdx.graphics.Color
//import com.badlogic.gdx.math.Matrix4
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.*
//
///** @author xoppa
// */
//class CollisionWorldTest : BaseBulletTest() {
//    var movingBox: BulletEntity? = null
//    var hit = false
//    var normalColor = Color()
//    var other: btCollisionObject? = null
//
//    inner class TestContactResultCallback : ContactResultCallback() {
//        override fun addSingleResult(
//            cp: btManifoldPoint, colObj0Wrap: btCollisionObjectWrapper, partId0: Int, index0: Int,
//            colObj1Wrap: btCollisionObjectWrapper, partId1: Int, index1: Int
//        ): Float {
//            hit = true
//            other =
//                if (colObj0Wrap.collisionObject === movingBox!!.body) colObj1Wrap.collisionObject else colObj0Wrap.collisionObject
//            return 0f
//        }
//    }
//
//    var contactCB: TestContactResultCallback? = null
//    override fun createWorld(): BulletWorld? {
//        val collisionConfig = btDefaultCollisionConfiguration()
//        val dispatcher = btCollisionDispatcher(collisionConfig)
//        val broadphase = btDbvtBroadphase()
//        val collisionWorld = btCollisionWorld(dispatcher, broadphase, collisionConfig)
//        return BulletWorld(collisionConfig, dispatcher, broadphase, null, collisionWorld)
//    }
//
//    override fun create() {
//        super.create()
//        instructions = "Long press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//        contactCB = TestContactResultCallback()
//        val groundModel = world!!.getConstructor("ground").model
//        val boxModel = world!!.getConstructor("box").model
//        world!!.addConstructor("collisionGround", BulletConstructor(groundModel))
//        world!!.addConstructor("collisionBox", BulletConstructor(boxModel))
//        world!!.add("collisionGround", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        world!!.add("collisionBox", 0f, 1f, 5f)!!
//            .setColor(
//                0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                0.5f + 0.5f * Math.random().toFloat(), 1f
//            )
//        world!!.add("collisionBox", 0f, 1f, -5f)!!
//            .setColor(
//                0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                0.5f + 0.5f * Math.random().toFloat(), 1f
//            )
//        world!!.add("collisionBox", 5f, 1f, 0f)!!
//            .setColor(
//                0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                0.5f + 0.5f * Math.random().toFloat(), 1f
//            )
//        world!!.add("collisionBox", -5f, 1f, 0f)!!
//            .setColor(
//                0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                0.5f + 0.5f * Math.random().toFloat(), 1f
//            )
//        movingBox = world!!.add("collisionBox", -5f, 1f, 0f)
//        normalColor[0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random()
//            .toFloat(), 0.5f + 0.5f * Math.random().toFloat()] = 1f
//    }
//
//    var tmpColor = Color()
//    override fun render() {
//        movingBox!!.transform!!.`val`[Matrix4.M23] = 0f
//        movingBox!!.transform!!.`val`[Matrix4.M13] = movingBox!!.transform!!.`val`[Matrix4.M23]
//        movingBox!!.transform!!.`val`[Matrix4.M03] = movingBox!!.transform!!.`val`[Matrix4.M13]
//        movingBox!!.transform!!.rotate(Vector3.Y, Gdx.graphics.deltaTime * 45f)
//        movingBox!!.transform!!.translate(-5f, 1f, 0f)
//        movingBox!!.body!!.worldTransform = movingBox!!.transform
//        super.render()
//    }
//
//    override fun update() {
//        super.update()
//        // Not using dynamics, so update the collision world manually
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.start()
//        world!!.collisionWorld.performDiscreteCollisionDetection()
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.stop()
//    }
//
//    override fun renderWorld() {
//        hit = false
//        other = null
//        world!!.collisionWorld.contactTest(movingBox!!.body, contactCB)
//        movingBox!!.color = if (hit) Color.RED else normalColor
//        var e: BulletEntity? = null
//        if (other != null && other.userData != null && other.userData is BulletEntity) {
//            e = other.userData as BulletEntity
//            tmpColor.set(e!!.color)
//            e.color = Color.RED
//        }
//        super.renderWorld()
//        if (e != null) e.color = tmpColor
//    }
//
//    override fun dispose() {
//        super.dispose()
//        movingBox = null
//    }
//}