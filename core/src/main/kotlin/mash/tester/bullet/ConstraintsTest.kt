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
//import com.badlogic.gdx.graphics.*
//import com.badlogic.gdx.graphics.g3d.Material
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
//import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint
//import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
//import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint
//import com.badlogic.gdx.utils.Array
//
///** @author xoppa
// */
//class ConstraintsTest : BaseBulletTest() {
//    val constraints = Array<btTypedConstraint>()
//    override fun create() {
//        super.create()
//        val barModel = modelBuilder.createBox(
//            10f,
//            1f,
//            1f,
//            Material(ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)),
//            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )
//        disposables.add(barModel)
//        world!!.addConstructor("bar", BulletConstructor(barModel, 0f)) // mass = 0: static body
//
//        // Create the entities
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        val bar = world!!.add("bar", 0f, 7f, 0f)
//        bar!!.setColor(
//            0.75f + 0.25f * Math.random().toFloat(), 0.75f + 0.25f * Math.random().toFloat(),
//            0.75f + 0.25f * Math.random().toFloat(), 1f
//        )
//        var box1 = world!!.add("box", -4.5f, 6f, 0f)
//        box1!!.setColor(
//            0.5f + 0.5f * Math.random().toFloat(),
//            0.5f + 0.5f * Math.random().toFloat(),
//            0.5f + 0.5f * Math.random().toFloat(),
//            1f
//        )
//        var constraint = btPoint2PointConstraint(
//            bar.body as btRigidBody,
//            box1.body as btRigidBody,
//            BaseBulletTest.Companion.tmpV1.set(-5f, -0.5f, -0.5f),
//            BaseBulletTest.Companion.tmpV2.set(-0.5f, 0.5f, -0.5f)
//        )
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(constraint, false)
//        constraints.add(constraint)
//        var box2: BulletEntity? = null
//        for (i in 0..9) {
//            if (i % 2 == 0) {
//                box2 = world!!.add("box", -3.5f + i.toFloat(), 6f, 0f)
//                box2!!.setColor(
//                    0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                    0.5f + 0.5f * Math.random().toFloat(), 1f
//                )
//                constraint = btPoint2PointConstraint(
//                    box1!!.body as btRigidBody,
//                    box2.body as btRigidBody,
//                    BaseBulletTest.Companion.tmpV1.set(0.5f, -0.5f, 0.5f),
//                    BaseBulletTest.Companion.tmpV2.set(-0.5f, -0.5f, 0.5f)
//                )
//            } else {
//                box1 = world!!.add("box", -3.5f + i.toFloat(), 6f, 0f)
//                box1!!.setColor(
//                    0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                    0.5f + 0.5f * Math.random().toFloat(), 1f
//                )
//                constraint = btPoint2PointConstraint(
//                    box2!!.body as btRigidBody,
//                    box1.body as btRigidBody,
//                    BaseBulletTest.Companion.tmpV1.set(0.5f, 0.5f, -0.5f),
//                    BaseBulletTest.Companion.tmpV2.set(-0.5f, 0.5f, -0.5f)
//                )
//            }
//            (world!!.collisionWorld as btDynamicsWorld).addConstraint(constraint, false)
//            constraints.add(constraint)
//        }
//        constraint = btPoint2PointConstraint(
//            bar.body as btRigidBody, box1!!.body as btRigidBody, BaseBulletTest.Companion.tmpV1.set(5f, -0.5f, -0.5f),
//            BaseBulletTest.Companion.tmpV2.set(0.5f, 0.5f, -0.5f)
//        )
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(constraint, false)
//        constraints.add(constraint)
//    }
//
//    override fun dispose() {
//        for (i in 0 until constraints.size) {
//            (world!!.collisionWorld as btDynamicsWorld).removeConstraint(constraints[i])
//            constraints[i].dispose()
//        }
//        constraints.clear()
//        super.dispose()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//}