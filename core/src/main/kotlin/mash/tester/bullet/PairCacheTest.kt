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
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.*
//
///** Based on FrustumCullingTest by Xoppa.
// *
// * @author jsjolund
// */
//class PairCacheTest : BaseBulletTest() {
//    private var useFrustumCam = false
//    private var ghostObject: btPairCachingGhostObject? = null
//    private var ghostEntity: BulletEntity? = null
//    private var manifoldArray: btPersistentManifoldArray? = null
//    private var angleX = 0f
//    private var angleY = 0f
//    private var angleZ = 0f
//    private var shapeRenderer: ShapeRenderer? = null
//    private var frustumCam: PerspectiveCamera? = null
//    private var overviewCam: PerspectiveCamera? = null
//    override fun create() {
//        super.create()
//        instructions =
//            "Tap to toggle view\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//        world!!.addConstructor("collisionBox", BulletConstructor(world!!.getConstructor("box").model))
//
//        // Create the entities
//        val dX = BOX_X_MAX - BOX_X_MIN
//        val dY = BOX_Y_MAX - BOX_Y_MIN
//        val dZ = BOX_Z_MAX - BOX_Z_MIN
//        for (i in 0 until BOXCOUNT) world!!.add(
//            "collisionBox",
//            BOX_X_MIN + dX * Math.random().toFloat(),
//            BOX_Y_MIN + dY * Math.random().toFloat(),
//            BOX_Z_MIN + dZ * Math.random().toFloat()
//        )!!.setColor(
//            0.25f + 0.5f * Math.random().toFloat(),
//            0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 1f
//        )
//        manifoldArray = btPersistentManifoldArray()
//        disposables.add(manifoldArray)
//        overviewCam = camera
//        overviewCam!!.position[BOX_X_MAX, BOX_Y_MAX] =
//            BOX_Z_MAX
//        overviewCam!!.lookAt(Vector3.Zero)
//        overviewCam!!.far = 150f
//        overviewCam!!.update()
//        frustumCam = PerspectiveCamera(camera!!.fieldOfView, camera!!.viewportWidth, camera!!.viewportHeight)
//        frustumCam!!.far = Vector3.len(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX)
//        frustumCam!!.update()
//        val ghostModel: Model = FrustumCullingTest.Companion.createFrustumModel(*frustumCam!!.frustum.planePoints)
//        disposables.add(ghostModel)
//
//        // The ghost object does not need to be shaped as a camera frustum, it can have any collision shape.
//        ghostObject = FrustumCullingTest.Companion.createFrustumObject(*frustumCam!!.frustum.planePoints)
//        disposables.add(ghostObject)
//        world!!.add(BulletEntity(ghostModel, ghostObject, 0f, 0f, 0f).also { ghostEntity = it })
//        disposables.add(ghostEntity)
//        shapeRenderer = ShapeRenderer()
//        disposables.add(shapeRenderer)
//    }
//
//    override fun createWorld(): BulletWorld? {
//        // No need to use dynamics for this test
//        val broadphase = btDbvtBroadphase()
//        val collisionConfig = btDefaultCollisionConfiguration()
//        val dispatcher = btCollisionDispatcher(collisionConfig)
//        val collisionWorld = btCollisionWorld(dispatcher, broadphase, collisionConfig)
//        return BulletWorld(collisionConfig, dispatcher, broadphase, null, collisionWorld)
//    }
//
//    override fun render() {
//        val dt = Gdx.graphics.deltaTime
//        ghostEntity!!.transform!!.idt()
//        ghostEntity!!.transform!!.rotate(Vector3.X, (angleX + dt * SPEED_X) % 360.also { angleX = it.toFloat() })
//        ghostEntity!!.transform!!.rotate(Vector3.Y, (angleY + dt * SPEED_Y) % 360.also { angleY = it.toFloat() })
//        ghostEntity!!.transform!!.rotate(Vector3.Z, (angleZ + dt * SPEED_Z) % 360.also { angleZ = it.toFloat() })
//
//        // Transform the ghost object
//        ghostEntity!!.body!!.worldTransform = ghostEntity!!.transform
//
//        // Transform the frustum cam
//        frustumCam!!.direction[0f, 0f] = -1f
//        frustumCam!!.up[0f, 1f] = 0f
//        frustumCam!!.position[0f, 0f] = 0f
//        frustumCam!!.rotate(ghostEntity!!.transform)
//        frustumCam!!.update()
//        super.render()
//
//        // Find all overlapping pairs which contain the ghost object and draw lines between the collision points.
//        shapeRenderer!!.projectionMatrix = camera!!.combined
//        shapeRenderer!!.begin(ShapeType.Line)
//        shapeRenderer!!.color = Color.WHITE
//        val arr = world!!.broadphase!!.overlappingPairCache.overlappingPairArray
//        val numPairs = arr.size()
//        for (i in 0 until numPairs) {
//            manifoldArray!!.clear()
//            val pair = arr.at(i)
//            val proxy0 = btBroadphaseProxy.obtain(pair.pProxy0.cPointer, false)
//            val proxy1 = btBroadphaseProxy.obtain(pair.pProxy1.cPointer, false)
//            val collisionPair = world!!.collisionWorld.pairCache.findPair(proxy0, proxy1) ?: continue
//            val algorithm = collisionPair.algorithm
//            algorithm?.getAllContactManifolds(manifoldArray)
//            for (j in 0 until manifoldArray!!.size()) {
//                val manifold = manifoldArray!!.atConst(j)
//                val isFirstBody = manifold.body0 === ghostObject
//                val otherObjectIndex = if (isFirstBody) manifold.body1.userValue else manifold.body0.userValue
//                val otherObjectColor = world!!.entities[otherObjectIndex]!!.color
//                for (p in 0 until manifold.numContacts) {
//                    val pt = manifold.getContactPoint(p)
//                    if (pt.distance < 0f) {
//                        if (isFirstBody) {
//                            pt.getPositionWorldOnA(BaseBulletTest.Companion.tmpV2)
//                            pt.getPositionWorldOnB(BaseBulletTest.Companion.tmpV1)
//                        } else {
//                            pt.getPositionWorldOnA(BaseBulletTest.Companion.tmpV1)
//                            pt.getPositionWorldOnB(BaseBulletTest.Companion.tmpV2)
//                        }
//                        shapeRenderer!!.line(
//                            BaseBulletTest.Companion.tmpV1.x,
//                            BaseBulletTest.Companion.tmpV1.y,
//                            BaseBulletTest.Companion.tmpV1.z,
//                            BaseBulletTest.Companion.tmpV2.x,
//                            BaseBulletTest.Companion.tmpV2.y,
//                            BaseBulletTest.Companion.tmpV2.z,
//                            otherObjectColor,
//                            Color.WHITE
//                        )
//                    }
//                }
//            }
//            btBroadphaseProxy.free(proxy0)
//            btBroadphaseProxy.free(proxy1)
//        }
//        shapeRenderer!!.end()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        useFrustumCam = !useFrustumCam
//        camera = if (useFrustumCam) frustumCam else overviewCam
//        return true
//    }
//
//    override fun update() {
//        super.update()
//        // Not using dynamics, so update the collision world manually
//        world!!.collisionWorld.performDiscreteCollisionDetection()
//    }
//
//    companion object {
//        const val BOX_X_MIN = -25f
//        const val BOX_Y_MIN = -25f
//        const val BOX_Z_MIN = -25f
//        const val BOX_X_MAX = 25f
//        const val BOX_Y_MAX = 25f
//        const val BOX_Z_MAX = 25f
//        const val SPEED_X = 360f / 7f
//        const val SPEED_Y = 360f / 19f
//        const val SPEED_Z = 360f / 13f
//        const val BOXCOUNT = 100
//    }
//}