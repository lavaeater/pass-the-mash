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
//import com.badlogic.gdx.graphics.GL20
//import com.badlogic.gdx.graphics.PerspectiveCamera
//import com.badlogic.gdx.graphics.VertexAttributes
//import com.badlogic.gdx.graphics.g3d.Material
//import com.badlogic.gdx.graphics.g3d.Model
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
//import com.badlogic.gdx.math.Matrix4
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.*
//import com.badlogic.gdx.utils.Array
//
///** @author Xoppa
// */
//class FrustumCullingTest : BaseBulletTest() {
//    var state = 0 // 0 = No culling, look from above
//    private var angleX = 0f
//    private var angleY = 0f
//    private var angleZ = 0f
//    private var frustumObject: btPairCachingGhostObject? = null
//    private var frustumEntity: BulletEntity? = null
//    private val visibleEntities = Array<BulletEntity?>()
//    private var tempManifoldArr: btPersistentManifoldArray? = null
//    private var frustumCam: PerspectiveCamera? = null
//    private var overviewCam: PerspectiveCamera? = null
//    override fun create() {
//        super.create()
//        instructions =
//            "Tap to toggle view\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//        tempManifoldArr = btPersistentManifoldArray()
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
//        )!!.color = Color.GRAY
//        frustumCam = PerspectiveCamera(camera!!.fieldOfView, camera!!.viewportWidth, camera!!.viewportHeight)
//        frustumCam!!.far = Vector3.len(BOX_X_MAX, BOX_Y_MAX, BOX_Z_MAX)
//        frustumCam!!.update()
//        overviewCam = camera
//        overviewCam!!.position[BOX_X_MAX, BOX_Y_MAX] = BOX_Z_MAX
//        overviewCam!!.lookAt(Vector3.Zero)
//        overviewCam!!.far = 150f
//        overviewCam!!.update()
//        val frustumModel = createFrustumModel(*frustumCam!!.frustum.planePoints)
//        disposables.add(frustumModel)
//        frustumObject = createFrustumObject(*frustumCam!!.frustum.planePoints)
//        world!!.add(BulletEntity(frustumModel, frustumObject, 0f, 0f, 0f).also { frustumEntity = it })
//        frustumEntity!!.color = Color.BLUE
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
//    override fun update() {
//        super.update()
//        // Not using dynamics, so update the collision world manually
//        if (USE_BULLET_FRUSTUM_CULLING) {
//            if (world!!.performanceCounter != null) world!!.performanceCounter!!.start()
//            world!!.collisionWorld.performDiscreteCollisionDetection()
//            if (world!!.performanceCounter != null) world!!.performanceCounter!!.stop()
//        }
//    }
//
//    override fun render() {
//        val dt = Gdx.graphics.deltaTime
//        frustumEntity!!.transform!!.idt()
//        frustumEntity!!.transform!!.rotate(Vector3.X, (angleX + dt * SPEED_X) % 360.also { angleX = it.toFloat() })
//        frustumEntity!!.transform!!.rotate(Vector3.Y, (angleY + dt * SPEED_Y) % 360.also { angleY = it.toFloat() })
//        frustumEntity!!.transform!!.rotate(Vector3.Z, (angleZ + dt * SPEED_Z) % 360.also { angleZ = it.toFloat() })
//
//        // Transform the ghost object
//        frustumEntity!!.body!!.worldTransform = frustumEntity!!.transform
//        // Transform the frustum cam
//        frustumCam!!.direction[0f, 0f] = -1f
//        frustumCam!!.up[0f, 1f] = 0f
//        frustumCam!!.position[0f, 0f] = 0f
//        frustumCam!!.rotate(frustumEntity!!.transform)
//        frustumCam!!.update()
//        super.render()
//        performance.append(" visible: ").append(visibleEntities.size)
//    }
//
//    override fun renderWorld() {
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.start()
//        if (USE_BULLET_FRUSTUM_CULLING) getEntitiesCollidingWithObject(
//            world!!, frustumObject, visibleEntities, tempManifoldArr
//        ) else {
//            visibleEntities.clear()
//            for (i in 0 until world!!.entities.size) {
//                val e = world!!.entities[i]!!
//                if (e === frustumEntity) continue
//                e.modelInstance!!.transform.getTranslation(tmpV)
//                if (frustumCam!!.frustum.sphereInFrustum(tmpV, 1f)) visibleEntities.add(e)
//            }
//        }
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.stop()
//        for (i in 0 until visibleEntities.size) visibleEntities[i]!!.color = Color.RED
//        modelBatch!!.begin(camera)
//        if (state and CULL_FRUSTUM == CULL_FRUSTUM) {
//            world!!.render(modelBatch!!, environment, visibleEntities)
//            world!!.render(modelBatch!!, environment, frustumEntity)
//        } else world!!.render(modelBatch!!, environment)
//        modelBatch!!.end()
//        for (i in 0 until visibleEntities.size) visibleEntities[i]!!.color = Color.GRAY
//    }
//
//    override fun beginRender(lighting: Boolean) {
//        super.beginRender(false)
//    }
//
//    override fun dispose() {
//        frustumObject = null
//        super.dispose()
//        if (tempManifoldArr != null) tempManifoldArr!!.dispose()
//        tempManifoldArr = null
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        state = (state + 1) % 3
//        camera =
//            if (state and FRUSTUM_CAM == FRUSTUM_CAM) frustumCam else overviewCam
//        return true
//    }
//
//    // Simple helper class to keep a reference to the collision shape
//    class TestPairCachingGhostObject : btPairCachingGhostObject() {
//        var shape: btCollisionShape? = null
//        override fun setCollisionShape(collisionShape: btCollisionShape) {
//            shape = collisionShape
//            super.setCollisionShape(collisionShape)
//        }
//
//        override fun dispose() {
//            super.dispose()
//            if (shape != null) shape!!.dispose()
//            shape = null
//        }
//    }
//
//    companion object {
//        /** Only show entities inside the frustum  */
//        const val CULL_FRUSTUM = 1
//
//        /** Transform the render cam with the frustum  */
//        const val FRUSTUM_CAM = 2
//        const val USE_BULLET_FRUSTUM_CULLING = true
//        const val BOXCOUNT = 200
//        const val BOX_X_MIN = -25f
//        const val BOX_Y_MIN = -25f
//        const val BOX_Z_MIN = -25f
//        const val BOX_X_MAX = 25f
//        const val BOX_Y_MAX = 25f
//        const val BOX_Z_MAX = 25f
//        const val SPEED_X = 360f / 7f
//        const val SPEED_Y = 360f / 19f
//        const val SPEED_Z = 360f / 13f
//        val tmpV = Vector3()
//        val tmpM = Matrix4()
//        val ptrs = IntArray(512)
//        val visibleObjects = Array<btCollisionObject>()
//        fun createFrustumObject(vararg points: Vector3?): btPairCachingGhostObject {
//            val result: btPairCachingGhostObject = TestPairCachingGhostObject()
//            val USE_COMPOUND = true
//            // Using a compound shape is not necessary, but it's good practice to create shapes around the center.
//            if (USE_COMPOUND) {
//                val centerNear = Vector3(points[2]).sub(points[0]).scl(0.5f).add(points[0])
//                val centerFar = Vector3(points[6]).sub(points[4]).scl(0.5f).add(points[4])
//                val center = Vector3(centerFar).sub(centerNear).scl(0.5f).add(centerNear)
//                val hullShape = btConvexHullShape()
//                for (i in points.indices) hullShape.addPoint(tmpV.set(points[i]).sub(center))
//                val shape = btCompoundShape()
//                shape.addChildShape(tmpM.setToTranslation(center), hullShape)
//                result.collisionShape = shape
//            } else {
//                val shape = btConvexHullShape()
//                for (i in points.indices) shape.addPoint(points[i])
//                result.collisionShape = shape
//            }
//            result.collisionFlags = btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE
//            return result
//        }
//
//        fun getEntitiesCollidingWithObject(
//            world: BulletWorld, `object`: btCollisionObject?,
//            out: Array<BulletEntity?>, tmpArr: btPersistentManifoldArray?
//        ): Array<BulletEntity?> {
//            // Fetch the array of contacts
//            val arr = world.broadphase!!.overlappingPairCache.overlappingPairArray
//            // Get the user values (which are indices in the entities array) of all objects colliding with the object
//            val n = arr.getCollisionObjectsValue(ptrs, `object`)
//            // Fill the array of entities
//            out.clear()
//            for (i in 0 until n) out.add(world.entities[ptrs[i]])
//            return out
//        }
//
//        fun createFrustumModel(vararg p: Vector3): Model {
//            val builder = ModelBuilder()
//            builder.begin()
//            val mpb = builder.part(
//                "", GL20.GL_LINES, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong(),
//                Material(ColorAttribute(ColorAttribute.Diffuse, Color.WHITE))
//            )
//            mpb.vertex(
//                p[0].x,
//                p[0].y,
//                p[0].z,
//                0f,
//                0f,
//                1f,
//                p[1].x,
//                p[1].y,
//                p[1].z,
//                0f,
//                0f,
//                1f,
//                p[2].x,
//                p[2].y,
//                p[2].z,
//                0f,
//                0f,
//                1f,
//                p[3].x,
//                p[3].y,
//                p[3].z,
//                0f,
//                0f,
//                1f,  // near
//                p[4].x,
//                p[4].y,
//                p[4].z,
//                0f,
//                0f,
//                -1f,
//                p[5].x,
//                p[5].y,
//                p[5].z,
//                0f,
//                0f,
//                -1f,
//                p[6].x,
//                p[6].y,
//                p[6].z,
//                0f,
//                0f,
//                -1f,
//                p[7].x,
//                p[7].y,
//                p[7].z,
//                0f,
//                0f,
//                -1f
//            )
//            mpb.index(
//                0.toShort(),
//                1.toShort(),
//                1.toShort(),
//                2.toShort(),
//                2.toShort(),
//                3.toShort(),
//                3.toShort(),
//                0.toShort()
//            )
//            mpb.index(
//                4.toShort(),
//                5.toShort(),
//                5.toShort(),
//                6.toShort(),
//                6.toShort(),
//                7.toShort(),
//                7.toShort(),
//                4.toShort()
//            )
//            mpb.index(
//                0.toShort(),
//                4.toShort(),
//                1.toShort(),
//                5.toShort(),
//                2.toShort(),
//                6.toShort(),
//                3.toShort(),
//                7.toShort()
//            )
//            return builder.end()
//        }
//    }
//}