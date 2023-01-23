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
//import com.badlogic.gdx.graphics.VertexAttributes
//import com.badlogic.gdx.graphics.g3d.Material
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.math.Matrix4
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.*
//import com.badlogic.gdx.utils.Array
//
//class ContactCacheTest : BaseBulletTest() {
//    class TestContactListener : ContactListener() {
//        var entities: Array<BulletEntity?>? = null
//        override fun onContactStarted(userValue0: Int, match0: Boolean, userValue1: Int, match1: Boolean) {
//            if (match0) {
//                entities!![userValue0]!!.color = Color.RED
//                Gdx.app.log(java.lang.Float.toString(time), "Contact started $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.RED
//                Gdx.app.log(java.lang.Float.toString(time), "Contact started $userValue1")
//            }
//        }
//
//        override fun onContactEnded(userValue0: Int, match0: Boolean, userValue1: Int, match1: Boolean) {
//            if (match0) {
//                entities!![userValue0]!!.color = Color.BLUE
//                Gdx.app.log(java.lang.Float.toString(time), "Contact ended $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.BLUE
//                Gdx.app.log(java.lang.Float.toString(time), "Contact ended $userValue1")
//            }
//        }
//    }
//
//    class TestContactCache : ContactCache() {
//        var entities: Array<BulletEntity?>? = null
//        override fun onContactStarted(manifold: btPersistentManifold, match0: Boolean, match1: Boolean) {
//            val userValue0 = manifold.body0.userValue
//            val userValue1 = manifold.body1.userValue
//            if (match0) {
//                entities!![userValue0]!!.color = Color.RED
//                Gdx.app.log(java.lang.Float.toString(time), "Contact started $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.RED
//                Gdx.app.log(java.lang.Float.toString(time), "Contact started $userValue1")
//            }
//        }
//
//        override fun onContactEnded(
//            colObj0: btCollisionObject,
//            match0: Boolean,
//            colObj1: btCollisionObject,
//            match1: Boolean
//        ) {
//            val userValue0 = colObj0.userValue
//            val userValue1 = colObj1.userValue
//            if (match0) {
//                entities!![userValue0]!!.color = Color.BLUE
//                Gdx.app.log(java.lang.Float.toString(time), "Contact ended $userValue0")
//            }
//            if (match1) {
//                entities!![userValue1]!!.color = Color.BLUE
//                Gdx.app.log(java.lang.Float.toString(time), "Contact ended $userValue1")
//            }
//        }
//    }
//
//    val SPHERECOUNT_X = 4
//    val SPHERECOUNT_Y = 1
//    val SPHERECOUNT_Z = 4
//    val SPHEREOFFSET_X = -2f
//    val SPHEREOFFSET_Y = 10f
//    val SPHEREOFFSET_Z = -2f
//    val USE_CONTACT_CACHE = true
//    var contactListener: TestContactListener? = null
//    var contactCache: TestContactCache? = null
//    override fun create() {
//        super.create()
//        val sphereModel = modelBuilder.createSphere(
//            1f, 1f, 1f, 8, 8,
//            Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)),
//            (
//                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )
//        disposables.add(sphereModel)
//        val sphereConstructor = BulletConstructor(sphereModel, 0.5f, btSphereShape(0.5f))
//        sphereConstructor.bodyInfo!!.restitution = 1f
//        world!!.addConstructor("sphere", sphereConstructor)
//        val sceneModel = objLoader.loadModel(Gdx.files.internal("data/scene.obj"))
//        disposables.add(sceneModel)
//        val sceneConstructor = BulletConstructor(
//            sceneModel, 0f,
//            btBvhTriangleMeshShape(sceneModel.meshParts)
//        )
//        sceneConstructor.bodyInfo!!.restitution = 0.25f
//        world!!.addConstructor("scene", sceneConstructor)
//        val scene = world!!.add(
//            "scene", Matrix4()
//                .setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, -90f)
//        )
//        scene!!.setColor(
//            0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//            0.25f + 0.5f * Math.random().toFloat(), 1f
//        )
//        scene.body!!.contactCallbackFlag = 2
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        for (x in 0 until SPHERECOUNT_X) {
//            for (y in 0 until SPHERECOUNT_Y) {
//                for (z in 0 until SPHERECOUNT_Z) {
//                    val e = world!!.add(
//                        "sphere", SPHEREOFFSET_X + x * 3f, SPHEREOFFSET_Y + y * 3f,
//                        SPHEREOFFSET_Z + z * 3f
//                    ) as BulletEntity
//                    e.setColor(
//                        0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(),
//                        0.5f + 0.5f * Math.random().toFloat(), 1f
//                    )
//                    e.body!!.contactCallbackFilter = 2
//                }
//            }
//        }
//        if (USE_CONTACT_CACHE) {
//            contactCache = TestContactCache()
//            contactCache!!.entities = world!!.entities
//            contactCache!!.cacheTime = 0.5f
//        } else {
//            contactListener = TestContactListener()
//            contactListener!!.entities = world!!.entities
//        }
//        time = 0f
//    }
//
//    override fun update() {
//        val delta = Gdx.graphics.deltaTime
//        time += delta
//        super.update()
//        if (contactCache != null) contactCache!!.update(delta)
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
//        if (contactCache != null) contactCache!!.dispose()
//        contactCache = null
//        contactListener = null
//        super.dispose()
//    }
//
//    companion object {
//        var time = 0f
//    }
//}