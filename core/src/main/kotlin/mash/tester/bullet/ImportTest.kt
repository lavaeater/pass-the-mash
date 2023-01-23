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
//import com.badlogic.gdx.assets.loaders.ModelLoader
//import com.badlogic.gdx.graphics.g3d.Model
//import com.badlogic.gdx.graphics.g3d.ModelInstance
//import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
//import com.badlogic.gdx.math.Matrix4
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
//import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
//import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
//import com.badlogic.gdx.physics.bullet.extras.btBulletWorldImporter
//import com.badlogic.gdx.utils.JsonReader
//
//class ImportTest : BaseBulletTest() {
//    var importer: btBulletWorldImporter? = null
//    var model: Model? = null
//
//    inner class MyImporter(world: btDynamicsWorld?) : btBulletWorldImporter(world) {
//        override fun createRigidBody(
//            isDynamic: Boolean, mass: Float, startTransform: Matrix4, shape: btCollisionShape,
//            bodyName: String
//        ): btRigidBody {
//            val localInertia = Vector3()
//            if (mass > 0f) shape.calculateLocalInertia(mass, localInertia)
//            val result = btRigidBody(mass, null, shape, localInertia)
//            val nodeName = bodyName.split("_".toRegex(), limit = 2).toTypedArray()[0] + "_model"
//            val instance = ModelInstance(model, nodeName, true, true)
//            instance.transform.set(startTransform)
//            val entity = BulletEntity(instance, result)
//            world!!.add(entity)
//            return result
//        }
//    }
//
//    override fun create() {
//        super.create()
//        val g3djLoader: ModelLoader<*> = G3dModelLoader(JsonReader())
//        model = g3djLoader.loadModel(Gdx.files.internal("data/g3d/btscene1.g3dj"))
//        disposables.add(model)
//        importer = MyImporter(world!!.collisionWorld as btDynamicsWorld)
//        importer.loadFile(Gdx.files.internal("data/g3d/btscene1.bullet"))
//        camera!!.position[10f, 15f] = 20f
//        camera!!.up[0f, 1f] = 0f
//        camera!!.lookAt(-10f, 8f, 0f)
//        camera!!.update()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//
//    override fun dispose() {
//        super.dispose()
//        importer!!.deleteAllData()
//        importer!!.dispose()
//        importer = null
//    }
//}