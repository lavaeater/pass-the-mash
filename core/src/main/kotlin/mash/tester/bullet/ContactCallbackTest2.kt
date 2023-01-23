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
//import com.badlogic.gdx.physics.bullet.collision.ContactListener
//import com.badlogic.gdx.utils.Array
//
//class ContactCallbackTest2 : BaseBulletTest() {
//    class TestContactListener : ContactListener() {
//        var entities: Array<BulletEntity?>? = null
//        override fun onContactStarted(userValue0: Int, match0: Boolean, userValue1: Int, match1: Boolean) {
//            if (match0) {
//                entities!![userValue0]!!.color = Color.RED
//                Gdx.app.log("ContactCallbackTest", "Contact started $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.RED
//                Gdx.app.log("ContactCallbackTest", "Contact started $userValue1")
//            }
//        }
//
//        override fun onContactEnded(userValue0: Int, match0: Boolean, userValue1: Int, match1: Boolean) {
//            if (match0) {
//                entities!![userValue0]!!.color = Color.BLUE
//                Gdx.app.log("ContactCallbackTest", "Contact ended $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.BLUE
//                Gdx.app.log("ContactCallbackTest", "Contact ended $userValue1")
//            }
//        }
//    }
//
//    val BOXCOUNT_X = 5
//    val BOXCOUNT_Y = 1
//    val BOXCOUNT_Z = 5
//    val BOXOFFSET_X = -5f
//    val BOXOFFSET_Y = 0.5f
//    val BOXOFFSET_Z = -5f
//    var contactListener: TestContactListener? = null
//    override fun create() {
//        super.create()
//
//        // Create the entities
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        for (x in 0 until BOXCOUNT_X) {
//            for (y in 0 until BOXCOUNT_Y) {
//                for (z in 0 until BOXCOUNT_Z) {
//                    val e = world!!.add(
//                        "box", BOXOFFSET_X + x * 2f, BOXOFFSET_Y + y * 2f,
//                        BOXOFFSET_Z + z * 2f
//                    ) as BulletEntity
//                    e.setColor(
//                        0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                        0.5f + 0.5f * Math.random().toFloat(), 1f
//                    )
//                    e.body!!.contactCallbackFlag = 2
//                    e.body!!.contactCallbackFilter = 2
//                }
//            }
//        }
//
//        // Creating a contact listener, also enables that particular type of contact listener and sets it active.
//        contactListener = TestContactListener()
//        contactListener!!.entities = world!!.entities
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//
//    override fun dispose() {
//        // Deleting the active contact listener, also disables that particular type of contact listener.
//        if (contactListener != null) contactListener!!.dispose()
//        contactListener = null
//        super.dispose()
//    }
//}