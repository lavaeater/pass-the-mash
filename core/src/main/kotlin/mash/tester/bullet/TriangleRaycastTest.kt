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
//import com.badlogic.gdx.graphics.g3d.Model
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape
//import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray
//import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback
//import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback.EFlags
//import com.badlogic.gdx.physics.bullet.linearmath.btVector3
//
///** @author jsjolund
// */
//class TriangleRaycastTest : BaseBulletTest() {
//    private inner class MyTriangleRaycastCallback(from: Vector3?, to: Vector3?) : btTriangleRaycastCallback(from, to) {
//        var hitNormalLocal = Vector3()
//        var hitFraction = 1f
//        var partId = -1
//        var triangleIndex = -1
//        private val tmpSetFrom = btVector3()
//        private val tmpSetTo = btVector3()
//        fun clearReport() {
//            hitNormalLocal.setZero()
//            hitFraction = 1f
//            partId = -1
//            triangleIndex = -1
//        }
//
//        override fun setHitFraction(hitFraction: Float) {
//            super.setHitFraction(hitFraction)
//            this.hitFraction = hitFraction
//        }
//
//        override fun reportHit(hitNormalLocal: Vector3, hitFraction: Float, partId: Int, triangleIndex: Int): Float {
//            // The hit with lowest hitFraction is closest to the ray origin.
//            // We need to find the lowest hitFraction since the super class does not handle it for us.
//            if (hitFraction < this.hitFraction) {
//                this.hitNormalLocal.set(hitNormalLocal)
//                this.hitFraction = hitFraction
//                this.partId = partId
//                this.triangleIndex = triangleIndex
//            }
//            return hitFraction
//        }
//
//        fun setFrom(value: Vector3) {
//            tmpSetFrom.setValue(value.x, value.y, value.z)
//            super.setFrom(tmpSetFrom)
//        }
//
//        fun setTo(value: Vector3) {
//            tmpSetTo.setValue(value.x, value.y, value.z)
//            super.setTo(tmpSetTo)
//        }
//
//        override fun dispose() {
//            tmpSetFrom.dispose()
//            tmpSetTo.dispose()
//            super.dispose()
//        }
//    }
//
//    private var model: Model? = null
//    private var triangleShape: btBvhTriangleMeshShape? = null
//    private var triangleRaycastCallback: MyTriangleRaycastCallback? = null
//    private val selectedTriangleVertices = arrayOf(Vector3(), Vector3(), Vector3())
//    private var shapeRenderer: ShapeRenderer? = null
//    private val rayFrom = Vector3()
//    private val rayTo = Vector3()
//    override fun create() {
//        super.create()
//        instructions =
//            "Tap a triangle to ray cast\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//        shapeRenderer = ShapeRenderer()
//        model = objLoader.loadModel(Gdx.files.internal("data/scene.obj"))
//        model.materials[0].clear()
//        model.materials[0][ColorAttribute.createDiffuse(Color.WHITE)] =
//            ColorAttribute.createSpecular(Color.WHITE)
//
//        // Only indexed BvhTriangleMeshShape can be used for triangle picking.
//        val vertexArray = btTriangleIndexVertexArray(model.meshParts)
//        triangleShape = btBvhTriangleMeshShape(vertexArray, true)
//        triangleRaycastCallback = MyTriangleRaycastCallback(Vector3.Zero, Vector3.Zero)
//        // Ignore intersection with mesh backfaces.
//        triangleRaycastCallback!!.flags = EFlags.kF_FilterBackfaces.toLong()
//        world!!.addConstructor("scene", BulletConstructor(model, 0f, triangleShape))
//        world!!.add("scene", 0f, 0f, 0f)
//        disposables.add(model)
//        disposables.add(triangleRaycastCallback)
//        disposables.add(triangleShape)
//        disposables.add(vertexArray)
//        disposables.add(shapeRenderer)
//    }
//
//    override fun render() {
//        super.render()
//        Gdx.gl.glLineWidth(5f)
//        shapeRenderer!!.projectionMatrix = camera!!.combined
//        shapeRenderer!!.begin(ShapeType.Line)
//        shapeRenderer!!.setColor(1f, 0f, 0f, 1f)
//        shapeRenderer!!.line(selectedTriangleVertices[0], selectedTriangleVertices[1])
//        shapeRenderer!!.line(selectedTriangleVertices[1], selectedTriangleVertices[2])
//        shapeRenderer!!.line(selectedTriangleVertices[2], selectedTriangleVertices[0])
//        shapeRenderer!!.end()
//        Gdx.gl.glLineWidth(1f)
//    }
//
//    override fun tap(screenX: Float, screenY: Float, count: Int, button: Int): Boolean {
//        val ray = camera!!.getPickRay(screenX, screenY)
//        rayFrom.set(ray.origin)
//        rayTo.set(ray.direction).scl(100f).add(rayFrom)
//
//        // Clean the callback object for reuse.
//        triangleRaycastCallback!!.setHitFraction(1f)
//        triangleRaycastCallback!!.clearReport()
//        triangleRaycastCallback!!.setFrom(rayFrom)
//        triangleRaycastCallback!!.setTo(rayTo)
//
//        // Ray casting is performed directly on the collision shape.
//        // The callback specifies the intersected MeshPart as well as triangle.
//        triangleShape!!.performRaycast(triangleRaycastCallback, rayFrom, rayTo)
//        val currentTriangleIndex = triangleRaycastCallback!!.triangleIndex
//        val currentPartId = triangleRaycastCallback!!.partId
//        if (currentTriangleIndex == -1 || currentPartId == -1) {
//            // No intersection was found.
//            return false
//        }
//
//        // Get the position coordinates of the vertices belonging to intersected triangle.
//        val mesh = model!!.meshParts[currentPartId].mesh
//        val verticesBuffer = mesh.getVerticesBuffer(false)
//        val indicesBuffer = mesh.getIndicesBuffer(false)
//        val posOffset = mesh.vertexAttributes.findByUsage(VertexAttributes.Usage.Position).offset / 4
//        val vertexSize = mesh.vertexSize / 4
//        val currentTriangleFirstVertexIndex = currentTriangleIndex * 3
//
//        // Store the three vertices belonging to the selected triangle.
//        for (i in 0..2) {
//            val currentVertexIndex = indicesBuffer[currentTriangleFirstVertexIndex + i].toInt() and 0xFFFF
//            var j = currentVertexIndex * vertexSize + posOffset
//            val x = verticesBuffer[j++]
//            val y = verticesBuffer[j++]
//            val z = verticesBuffer[j]
//            selectedTriangleVertices[i][x, y] = z
//        }
//        return true
//    }
//}