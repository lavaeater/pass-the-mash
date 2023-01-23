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
//import com.badlogic.gdx.graphics.Color
//import com.badlogic.gdx.physics.bullet.collision.ContactResultCallback
//import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper
//import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint
//import com.badlogic.gdx.utils.Array
//import com.badlogic.gdx.utils.Pool
//
///** @author Xoppa
// */
//class CollisionTest : ShootTest() {
//    var projectile: BulletEntity? = null
//    var hits = Array<BulletEntity>()
//    var contacts = Array<BulletEntity>()
//    var colors = Array<Color>()
//
//    inner class TestContactResultCallback : ContactResultCallback() {
//        override fun addSingleResult(
//            cp: btManifoldPoint, colObj0Wrap: btCollisionObjectWrapper, partId0: Int, index0: Int,
//            colObj1Wrap: btCollisionObjectWrapper, partId1: Int, index1: Int
//        ): Float {
//            val other =
//                if (colObj0Wrap.collisionObject === projectile!!.body) colObj1Wrap.collisionObject else colObj0Wrap.collisionObject
//            if (other != null && other.userData != null && other.userData is BulletEntity) {
//                val ent = other.userData as BulletEntity
//                if (ent !== ground && !hits.contains(ent, true)) hits.add(other.userData as BulletEntity)
//            }
//            return 0f
//        }
//    }
//
//    var contactCB: TestContactResultCallback? = null
//    fun updateContactInfo() {
//        val n = world!!.dispatcher!!.numManifolds
//        for (i in 0 until n) {
//            val manifold = world!!.dispatcher!!.getManifoldByIndexInternal(i)
//            val objA = manifold.body0
//            val objB = manifold.body1
//            if (objA !== ground!!.body && objB !== ground!!.body) {
//                if (objA.userData != null && objA.userData is BulletEntity) {
//                    val ent = objA.userData as BulletEntity
//                    if (ent !== projectile && !contacts.contains(ent, true) && !hits.contains(ent, true)) contacts.add(
//                        ent
//                    )
//                }
//                if (objB.userData != null && objB.userData is BulletEntity) {
//                    val ent = objB.userData as BulletEntity
//                    if (ent !== projectile && !contacts.contains(ent, true) && !hits.contains(ent, true)) contacts.add(
//                        ent
//                    )
//                }
//            }
//        }
//    }
//
//    override fun create() {
//        super.create()
//        contactCB = TestContactResultCallback()
//    }
//
//    override fun render() {
//        process()
//    }
//
//    private val colorPool: Pool<Color> = object : Pool<Color>() {
//        override fun newObject(): Color {
//            return Color()
//        }
//    }
//
//    fun process() {
//        var color: Color? = null
//        update()
//        hits.clear()
//        contacts.clear()
//
//        // Note that this might miss collisions, use InternalTickCallback to check for collision on every tick.
//        // See InternalTickTest on how to implement it.
//
//        // Check what the projectile hits
//        if (projectile != null) {
//            color = projectile!!.color
//            projectile!!.color = Color.RED
//            world!!.collisionWorld.contactTest(projectile!!.body, contactCB)
//        }
//        // Check for other collisions
//        updateContactInfo()
//        if (hits.size > 0) {
//            for (i in 0 until hits.size) {
//                colors.add(colorPool.obtain().set(hits[i].color))
//                hits[i].color = Color.RED
//            }
//        }
//        if (contacts.size > 0) {
//            for (i in 0 until contacts.size) {
//                colors.add(colorPool.obtain().set(contacts[i].color))
//                contacts[i].color = Color.BLUE
//            }
//        }
//        render(false)
//        if (projectile != null) projectile!!.color = color!!
//        for (i in 0 until hits.size) hits[i].color = colors[i]
//        for (i in 0 until contacts.size) contacts[i].color = colors[hits.size + i]
//        colorPool.freeAll(colors)
//        colors.clear()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        projectile = shoot(x, y)
//        return true
//    }
//
//    override fun dispose() {
//        super.dispose()
//        projectile = null
//    }
//}