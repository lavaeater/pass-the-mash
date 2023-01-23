/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mash.tester.bullet

import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import mash.tester.bullet.OcclusionBuffer
import java.nio.Buffer
import java.nio.FloatBuffer

/** Software rasterizer used for depth rendering and testing of bounding box triangles. Stores depth values inside a
 * [FloatBuffer]. CPU rendering is used in order to avoid the frequent GPU to CPU synchronization which would be needed if
 * hardware rendering were to be used for occlusion culling queries.
 *
 *
 * Based on the algorithm from the Bullet CDTestFramework, BulletSAPCompleteBoxPruningTest.cpp, written by Erwin Coumans.
 *
 * @author jsjolund
 */
class OcclusionBuffer(
    /** Width of depth buffer image in pixels  */
    val bufferWidth: Int,
    /** Height of depth buffer image in pixels  */
    val bufferHeight: Int
) : Disposable {
    internal class GridPoint3 : com.badlogic.gdx.math.GridPoint3() {
        fun add(other: GridPoint3): GridPoint3 {
            return set(x + other.x, y + other.y, z + other.z) as GridPoint3
        }
    }

    /** Determines actions and return values for triangle rasterization policies.  */
    private enum class Policy {
        DRAW, QUERY;

        /** Evaluate the positions of the vertices depending on policy.
         *
         * @param vertices Vertices in camera space
         * @return True if in query mode and any of the vertices are behind camera frustum near plane.
         */
        fun evaluate(vertices: Array<Quaternion?>): Boolean {
            return when (this) {
                DRAW -> false
                QUERY -> {
                    // If we are querying and any of the vertices are behind the camera, return true.
                    // This means a bounding box will not be considered occluded when any of its vertices
                    // are behind the camera frustum near plane.
                    for (vertex in vertices) {
                        if (vertex!!.z + vertex.w <= 0) return true
                    }
                    false
                }
            }
            return false
        }

        /** Compare the current value in the depth buffer with a new value. If draw policy is used, write the new value if it is
         * larger than current depth (closer to camera). If query is used, return true if new depth not occluded by old depth.
         *
         * @param depthBuffer The depth buffer
         * @param bufferIndex Index in buffer at which to compare depth
         * @param newDepth New value to compare with
         * @return True if in query mode and new value closer to the camera, false otherwise
         */
        fun process(depthBuffer: FloatBuffer, bufferIndex: Int, newDepth: Float): Boolean {
            val oldDepth = depthBuffer[bufferIndex]
            return when (this) {
                DRAW -> {
                    if (newDepth > oldDepth) depthBuffer.put(bufferIndex, newDepth)
                    false
                }

                QUERY -> newDepth >= oldDepth
            }
            return false
        }
    }

    internal class Quaternion : com.badlogic.gdx.math.Quaternion() {
        /** Left-multiplies the quaternion by the given matrix.
         * @param matrix The matrix
         * @return This vector for chaining
         */
        fun mul(matrix: Matrix4): Quaternion {
            val `val` = matrix.`val`
            return this.set(
                x * `val`[Matrix4.M00] + y * `val`[Matrix4.M01] + z * `val`[Matrix4.M02] + w * `val`[Matrix4.M03],
                x * `val`[Matrix4.M10] + y * `val`[Matrix4.M11] + z * `val`[Matrix4.M12] + w * `val`[Matrix4.M13],
                x * `val`[Matrix4.M20] + y * `val`[Matrix4.M21] + z * `val`[Matrix4.M22] + w * `val`[Matrix4.M23],
                x * `val`[Matrix4.M30] + y * `val`[Matrix4.M31] + z * `val`[Matrix4.M32] + w * `val`[Matrix4.M33]
            )
        }

        /** Multiply the x,y,z,w components of the passed in quaternion with the scalar and add them to the components of this
         * quaternion  */
        fun mulAdd(quaternion: Quaternion, scalar: Float): Quaternion {
            x += quaternion.x * scalar
            y += quaternion.y * scalar
            z += quaternion.z * scalar
            w += quaternion.w * scalar
            return this
        }

        override fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
            return super.set(x, y, z, w) as Quaternion
        }

        fun set(quaternion: Quaternion?): Quaternion {
            return super.set(quaternion) as Quaternion
        }

        /** Subtract the x,y,z,w components of the passed in quaternion to the ones of this quaternion  */
        fun sub(qx: Float, qy: Float, qz: Float, qw: Float): Quaternion {
            x -= qx
            y -= qy
            z -= qz
            w -= qw
            return this
        }

        /** Subtract the x,y,z,w components of the passed in quaternion to the ones of this quaternion  */
        fun sub(quaternion: Quaternion): Quaternion {
            x -= quaternion.x
            y -= quaternion.y
            z -= quaternion.z
            w -= quaternion.w
            return this
        }
    }

    /** Depth buffer  */
    private val buffer: FloatBuffer

    /** Half extents of depth buffer in pixels  */
    private val bufferHalfExt: Vector2

    /** Half extents of depth buffer offset by half a pixel  */
    private val bufferOffset: Vector2

    // Temporary storage
    private val box = arrayOfNulls<Vector3>(8)
    private val tmpVertices = arrayOfNulls<Quaternion>(8)
    private val clippedQuad = arrayOfNulls<Quaternion>(8)
    private val quad = arrayOfNulls<Quaternion>(4)
    private val tmpQ1 = Quaternion()
    private val tmpQ2 = Quaternion()
    private val tmpV1 = Vector3()
    private val tmpV2 = Vector3()
    private val triX = GridPoint3()
    private val triY = GridPoint3()
    private val triDX = GridPoint3()
    private val triDY = GridPoint3()
    private val cursor = GridPoint3()

    // Debug drawing
    private var debugPixmap: Pixmap? = null
    private var debugTexture: Texture? = null
    private var debugTextureRegion: TextureRegion? = null
    private val projectionMatrix = Matrix4()

    /** Creates a new [OcclusionBuffer]
     *
     * @param width Width of the buffer image
     * @param height Height of the buffer image
     */
    init {
        bufferHalfExt = Vector2(bufferWidth * 0.5f, bufferHeight * 0.5f)
        bufferOffset = Vector2(bufferHalfExt.x + 0.5f, bufferHalfExt.y + 0.5f)
        buffer = BufferUtils.newFloatBuffer(bufferWidth * bufferHeight)
        for (i in 0..7) {
            box[i] = Vector3()
            tmpVertices[i] = Quaternion()
            clippedQuad[i] = Quaternion()
        }
        for (i in 0..3) quad[i] = Quaternion()
    }

    /** Clears the depth buffer by setting the depth to -1.  */
    fun clear() {
        (buffer as Buffer).clear()
        while (buffer.position() < buffer.capacity()) buffer.put(-1f)
    }

    /** Clip a polygon with camera near plane if necessary.
     *
     * @param verticesIn Input
     * @param verticesOut Output
     * @return Number of vertices needed to draw the (clipped) face
     */
    private fun clipQuad(verticesIn: Array<Quaternion?>, verticesOut: Array<Quaternion?>): Int {
        val numVerts = verticesIn.size
        var numVertsBehind = 0
        val s = FloatArray(4)
        for (i in 0 until numVerts) {
            s[i] = verticesIn[i]!!.z + verticesIn[i]!!.w
            if (s[i] < 0) numVertsBehind++
        }
        return if (numVertsBehind == numVerts) {
            // All vertices outside frustum
            0
        } else if (numVertsBehind > 0) {
            // Some vertices are behind the camera, so perform clipping.
            var newNumVerts = 0
            var i = numVerts - 1
            var j = 0
            while (j < numVerts) {
                val a = tmpQ1.set(verticesIn[i])
                val b = tmpQ2.set(verticesIn[j])
                val t = s[i] / (a.w + a.z - b.w - b.z)
                if (t > 0 && t < 1) verticesOut[newNumVerts++]!!.set(a).mulAdd(b.sub(a), t)
                if (s[j] > 0) verticesOut[newNumVerts++]!!.set(verticesIn[j])
                i = j++
            }
            newNumVerts
        } else {
            // No clipping needed.
            for (i in 0 until numVerts) verticesOut[i]!!.set(verticesIn[i])
            numVerts
        }
    }

    override fun dispose() {
        if (debugPixmap != null) {
            debugPixmap!!.dispose()
            debugTexture!!.dispose()
            debugPixmap = null
            debugTexture = null
        }
    }

    /** Renders an AABB (axis aligned bounding box) to the depth buffer.
     *
     * @param center Center of AABB in world coordinates
     * @param halfExt Half extents of AABB
     */
    fun drawAABB(center: Vector3, halfExt: Vector3) {
        setAABBVertices(center, halfExt, box)
        drawBox(box, Policy.DRAW)
    }

    /** Renders a bounding box to the depth buffer. Does not need to be axis aligned, but will use the translation, rotation and
     * scale from the matrix parameter.
     *
     * @param worldTransform World transform of the box to render.
     * @param halfExt Half extents of the box.
     */
    fun drawBB(worldTransform: Matrix4, halfExt: Vector3) {
        val center = tmpV1.setZero()
        setAABBVertices(center, halfExt, box)
        worldTransform.getTranslation(center)
        for (vertex in box) {
            vertex!!.rot(worldTransform)
            vertex.add(center)
        }
        drawBox(box, Policy.DRAW)
    }

    /** Draws a bounding box to the depth buffer, or queries the depth buffer at the pixels the box occupies, depending on policy.
     *
     * @param vertices Vertices of box
     * @param policy Rasterization policy to use
     * @return True if query policy is used, and any part of the box passes a depth test. False otherwise.
     */
    private fun drawBox(vertices: Array<Vector3?>, policy: Policy): Boolean {
        for (i in 0..7) {
            // Multiply the world coordinates by the camera combined matrix, but do not divide by w component yet.
            val v = vertices[i]
            tmpVertices[i]!!.set(v!!.x, v.y, v.z, 1f).mul(projectionMatrix)
        }
        if (policy.evaluate(tmpVertices)) return true

        // Loop over each box quad in the predefined winding order.
        var i = 0
        while (i < WINDING.size) {
            quad[0]!!.set(tmpVertices[WINDING[i++]])
            quad[1]!!.set(tmpVertices[WINDING[i++]])
            quad[2]!!.set(tmpVertices[WINDING[i++]])
            quad[3]!!.set(tmpVertices[WINDING[i++]])
            // Clip the quad with near frustum plane if needed
            val numVertices = clipQuad(quad, clippedQuad)
            // Divide by w to project vertices to camera space
            for (j in 0 until numVertices) {
                val q = clippedQuad[j]
                q!!.z = 1 / q.w
                vertices[j]!![q.x * q.z, q.y * q.z] = q.z
            }
            // Perform draw/query
            for (j in 2 until numVertices) {
                // If we are querying and depth test passes, there is no need to continue the rasterization,
                // since part of the AABB must be visible then.
                if (drawTriangle(vertices[0], vertices[j - 1], vertices[j], policy)) return true
            }
        }
        return false
    }

    /** Draw the depth buffer to a texture. Slow, should only be used for debugging purposes.
     *
     * @return Region of debug texture
     */
    fun drawDebugTexture(): TextureRegion? {
        if (debugPixmap == null) {
            debugPixmap = Pixmap(bufferWidth, bufferHeight, Pixmap.Format.RGBA8888)
            debugTexture = Texture(debugPixmap)
            debugTextureRegion = TextureRegion(debugTexture)
            debugTextureRegion!!.flip(false, true)
        }
        debugPixmap!!.setColor(Color.BLACK)
        debugPixmap!!.fill()
        // Find min/max depth values in buffer
        var minDepth = Float.POSITIVE_INFINITY
        var maxDepth = Float.NEGATIVE_INFINITY
        (buffer as Buffer).clear()
        while (buffer.position() < buffer.capacity()) {
            val depth = MathUtils.clamp(buffer.get(), 0f, Float.POSITIVE_INFINITY)
            minDepth = Math.min(depth, minDepth)
            maxDepth = Math.max(depth, maxDepth)
        }
        val extent = 1 / (maxDepth - minDepth)
        (buffer as Buffer).clear()
        // Draw to pixmap
        for (x in 0 until bufferWidth) {
            for (y in 0 until bufferHeight) {
                val depth = MathUtils.clamp(buffer[x + y * bufferWidth], 0f, Float.POSITIVE_INFINITY)
                val c = depth * extent
                debugPixmap!!.drawPixel(x, y, Color.rgba8888(c, c, c, 1f))
            }
        }
        debugTexture!!.draw(debugPixmap, 0, 0)
        return debugTextureRegion
    }

    /** Rasterizes a triangle with linearly interpolated depth values.
     *
     *
     * If used with [Policy.DRAW] the triangle will be drawn to the depth buffer wherever it passes a depth test.
     *
     *
     * If [Policy.QUERY] is used, the depth values in the triangle will be compared with existing depth buffer values. If any
     * pixel passes a depth test the rasterization will be aborted and the method will return true.
     *
     * @param a Triangle vertex in camera space
     * @param b Triangle vertex in camera space
     * @param c Triangle vertex in camera space
     * @param policy Draw or query policy
     * @return With query policy, true if any pixel in the triangle passes a depth test. False otherwise.
     */
    private fun drawTriangle(a: Vector3?, b: Vector3?, c: Vector3?, policy: Policy): Boolean {
        // Check if triangle faces away from the camera (back-face culling).
        if (tmpV1.set(b).sub(a).crs(tmpV2.set(c).sub(a)).z <= 0) return false
        // Triangle coordinates and size.
        // Note that x, y, z in e.g. triX corresponds to x components of vertices a, b, c,
        // which means triX.x is the x coordinate of a.
        triX[(a!!.x * bufferHalfExt.x + bufferOffset.x).toInt(), (b!!.x * bufferHalfExt.x + bufferOffset.x).toInt()] =
            (c!!.x * bufferHalfExt.x + bufferOffset.x).toInt()
        triY[(a.y * bufferHalfExt.y + bufferOffset.y).toInt(), (b.y * bufferHalfExt.y + bufferOffset.y).toInt()] =
            (c.y * bufferHalfExt.y + bufferOffset.y).toInt()
        // X/Y extents
        val xMin = Math.max(0, Math.min(triX.x, Math.min(triX.y, triX.z)))
        val xMax = Math.min(bufferWidth, 1 + Math.max(triX.x, Math.max(triX.y, triX.z)))
        val yMin = Math.max(0, Math.min(triY.x, Math.min(triY.y, triY.z)))
        val yMax = Math.min(bufferWidth, 1 + Math.max(triY.x, Math.max(triY.y, triY.z)))
        val width = xMax - xMin
        val height = yMax - yMin
        if (width * height <= 0) return false
        // Cursor
        triDX[triY.x - triY.y, triY.y - triY.z] = triY.z - triY.x
        triDY[triX.y - triX.x - triDX.x * width, triX.z - triX.y - triDX.y * width] =
            triX.x - triX.z - triDX.z * width
        cursor[yMin * (triX.y - triX.x) + xMin * (triY.x - triY.y) + triX.x * triY.y - triX.y * triY.x, yMin * (triX.z - triX.y) + xMin * (triY.y - triY.z) + triX.y * triY.z - triX.z * triY.y] =
            yMin * (triX.x - triX.z) + xMin * (triY.z - triY.x) + triX.z * triY.x - triX.x * triY.z
        // Depth interpolation
        val ia = (1f
                / (triX.x * triY.y - triX.y * triY.x + triX.z * triY.x - triX.x * triY.z + triX.y * triY.z - triX.z * triY.y).toFloat())
        val dzx = ia * (triY.x * (c.z - b.z) + triY.y * (a.z - c.z) + triY.z * (b.z - a.z))
        val dzy = ia * (triX.x * (b.z - c.z) + triX.y * (c.z - a.z) + triX.z * (a.z - b.z)) - dzx * width
        var drawDepth = ia * (a.z * cursor.y + b.z * cursor.z + c.z * cursor.x)
        var bufferRow = yMin * bufferHeight
        // Loop over pixels and process the triangle pixel depth versus the existing value in buffer.
        for (iy in yMin until yMax) {
            for (ix in xMin until xMax) {
                val bufferIndex = bufferRow + ix
                if (cursor.x >= 0 && cursor.y >= 0 && cursor.z >= 0 && policy.process(
                        buffer,
                        bufferIndex,
                        drawDepth
                    )
                ) return true
                cursor.add(triDX)
                drawDepth += dzx
            }
            cursor.add(triDY)
            drawDepth += dzy
            bufferRow += bufferWidth
        }
        return false
    }

    /** Queries the depth buffer as to whether an AABB (axis aligned bounding box) is completely occluded by a previously rendered
     * object. If any part of the AABB is visible (not occluded), the method returns true.
     *
     * @param center Center of AABB in world coordinates
     * @param halfExt Half extents of AABB
     * @return True if any part of the AABB is visible, false otherwise.
     */
    fun queryAABB(center: Vector3, halfExt: Vector3): Boolean {
        setAABBVertices(center, halfExt, box)
        return drawBox(box, Policy.QUERY)
    }

    /** Sets the projection matrix to be used for rendering. Usually this will be set to Camera.combined.
     * @param matrix
     */
    fun setProjectionMatrix(matrix: Matrix4?) {
        projectionMatrix.set(matrix)
    }

    companion object {
        /** Face winding order of [.setAABBVertices]  */
        private val WINDING = intArrayOf(1, 0, 3, 2, 4, 5, 6, 7, 4, 7, 3, 0, 6, 5, 1, 2, 7, 6, 2, 3, 5, 4, 0, 1)

        /** Calculates the eight vertices of an AABB.
         *
         * @param center Center point
         * @param halfExt Half extents
         * @param vertices Vertices output
         */
        private fun setAABBVertices(center: Vector3, halfExt: Vector3, vertices: Array<Vector3?>) {
            vertices[0]!![center.x - halfExt.x, center.y - halfExt.y] = center.z - halfExt.z
            vertices[1]!![center.x + halfExt.x, center.y - halfExt.y] = center.z - halfExt.z
            vertices[2]!![center.x + halfExt.x, center.y + halfExt.y] = center.z - halfExt.z
            vertices[3]!![center.x - halfExt.x, center.y + halfExt.y] = center.z - halfExt.z
            vertices[4]!![center.x - halfExt.x, center.y - halfExt.y] = center.z + halfExt.z
            vertices[5]!![center.x + halfExt.x, center.y - halfExt.y] = center.z + halfExt.z
            vertices[6]!![center.x + halfExt.x, center.y + halfExt.y] = center.z + halfExt.z
            vertices[7]!![center.x - halfExt.x, center.y + halfExt.y] = center.z + halfExt.z
        }
    }
}