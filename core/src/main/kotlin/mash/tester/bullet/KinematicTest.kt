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
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.Collision
//import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
//
///** @author xoppa
// */
//class KinematicTest : BaseBulletTest() {
//    var kinematicBox: BulletEntity? = null
//    var kinematicBox1: BulletEntity? = null
//    var kinematicBox2: BulletEntity? = null
//    var kinematicBox3: BulletEntity? = null
//    var position: Vector3? = null
//    var angle = 0f
//    override fun create() {
//        super.create()
//
//        // Create the entities
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        kinematicBox1 = world!!.add("staticbox", position1.x, position1.y, position1.z)
//        kinematicBox1!!.color = Color.RED
//        kinematicBox1!!.body.collisionFlags =
//            kinematicBox1!!.body!!.collisionFlags or btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT
//        kinematicBox2 = world!!.add("staticbox", position2.x, position2.y, position2.z)
//        kinematicBox2!!.color = Color.RED
//        kinematicBox2!!.body.collisionFlags =
//            kinematicBox2!!.body!!.collisionFlags or btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT
//        kinematicBox3 = world!!.add("staticbox", position3.x, position3.y, position3.z)
//        kinematicBox3!!.color = Color.RED
//        kinematicBox3!!.body.collisionFlags =
//            kinematicBox3!!.body!!.collisionFlags or btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT
//        // This makes bullet call btMotionState#getWorldTransform on every update:
//        kinematicBox3!!.body!!.activationState = Collision.DISABLE_DEACTIVATION
//        angle = 360f
//    }
//
//    override fun render() {
//        angle = angle + Gdx.graphics.deltaTime * 360f / 5f
//        kinematicBox3!!.transform!!.idt().rotate(Vector3.Y, 360f - 2f * angle).translate(position3)
//        if (angle >= 360f) {
//            angle = 0f
//            kinematicBox = if (kinematicBox === kinematicBox1) kinematicBox2 else kinematicBox1
//            position = if (position === position1) position2 else position1
//        }
//        kinematicBox!!.transform!!.idt().rotate(Vector3.Y, angle).translate(position)
//        // This makes bullet call btMotionState#getWorldTransform once:
//        kinematicBox!!.body!!.activationState = Collision.ACTIVE_TAG
//        super.render()
//    }
//
//    override fun dispose() {
//        kinematicBox3 = null
//        kinematicBox2 = kinematicBox3
//        kinematicBox1 = kinematicBox2
//        kinematicBox = kinematicBox1
//        position = null
//        super.dispose()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//
//    companion object {
//        val position1 = Vector3(5f, 0.5f, 0f)
//        val position2 = Vector3(8f, 0.5f, 0f)
//        val position3 = Vector3(10f, 0.5f, 0f)
//    }
//}