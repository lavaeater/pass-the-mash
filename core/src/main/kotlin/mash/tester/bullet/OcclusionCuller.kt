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

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Frustum
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import java.nio.Buffer

/** Performs the occlusion culling or k-DOP culling using the dynamic bounding volume tree from the Bullet broadphase.
 *
 *
 * Occlusion culling is a process that determines which objects are visible or hidden from the viewpoint of a camera. This is
 * achieved by rendering the bounding boxes of collision objects to a depth buffer using a software rasterizer. When determining
 * if an object is visible to the camera, this depth buffer is queried and compared against the depth of the object in question.
 *
 *
 * k-DOP culling determines which objects are inside a camera frustum. The process is accelerated by the dynamic bounding volume
 * tree.
 *
 * @author jsjolund
 */
abstract class OcclusionCuller : Disposable {
    protected inner class Collider : ICollide() {
        /** Callback method for [btDbvt.collideKDOP]. The bounding volume tree node in the parameter is inside the camera
         * frustum, as are any collision objects it contains.
         *
         * @param node A bounding volume tree node, the children of which are all inside the camera frustum.
         */
        override fun AllLeaves(node: btDbvtNode): Boolean {
            if (node.isleaf()) {
                onObjectVisible(node.dataAsProxyClientObject)
            } else {
                val nodePointer = node.cPointer
                var child: btDbvtNode
                if (btDbvtNode.internalTemp(nodePointer, false).getChild(0)
                        .also { child = it }.cPointer != 0L
                ) AllLeaves(child)
                if (btDbvtNode.internalTemp(nodePointer, false).getChild(1)
                        .also { child = it }.cPointer != 0L
                ) AllLeaves(child)
            }
            return true
        }

        /** Callback for [btDbvt.collideOCL]. This method must check if the input node from the broadphase bounding volume
         * tree is completely occluded by an occluder node previously rendered by [.Process].
         *
         *
         * The node may or may not be a leaf node, i.e. may or may not contain a single collision object. If the node is not a leaf
         * (i.e. an internal node), and the node is occluded, the child nodes it contains will not be processed or checked for
         * occlusion, since they are contained in this parent node bounding volume and must therefore also be occluded.
         *
         * @param node A node from the broadphase bounding volume tree.
         * @return False if the node is completely occluded, true otherwise.
         */
        override fun Descent(node: btDbvtNode): Boolean {
            return oclBuffer!!.queryAABB(tmpV1.set(node.volume.Center()), tmpV2.set(node.volume.Extents()))
        }

        /** Callback for [btDbvt.collideOCL], which will trigger a call to this method if a leaf node is not occluded. If the
         * node contains an object which can occlude others, it will be added to the depth buffer so that it may be considered in
         * future occlusion checks.
         *
         *
         * Only box shaped occluder objects are supported.
         *
         * @param leaf A leaf node which contains a collision object.
         * @param depth The depth of the node along the sorting axis. Objects closer to the camera will have a value closer to
         * zero.
         */
        override fun Process(leaf: btDbvtNode, depth: Float) {
            val `object` = leaf.dataAsProxyClientObject
            onObjectVisible(`object`)
            val shape = `object`.collisionShape
            if (shape is btBoxShape && isOccluder(`object`)) {
                oclBuffer!!.drawBB(`object`.worldTransform, shape.halfExtentsWithMargin)
            }
        }
    }

    private val frustumNormals = BufferUtils.newFloatBuffer(NUM_PLANES * 4)
    private val frustumOffsets = BufferUtils.newFloatBuffer(NUM_PLANES)
    val tmpV1 = Vector3()
    val tmpV2 = Vector3()
    var oclBuffer: OcclusionBuffer? = null
    private val collider: Collider = Collider()
    override fun dispose() {
        collider.dispose()
        oclBuffer = null
    }

    /** True if this collision object can block vision of other collision objects. If true, its collision shape will be drawn to
     * the depth buffer and considered in future occlusion checks. Only btBoxShape collision shapes can be occluders, other types
     * of shapes will be ignored. However, box occluders can block vision of any type of collision shape.
     *
     * @param object Object to check
     * @return True if the collision object can occlude other objects
     */
    abstract fun isOccluder(`object`: btCollisionObject): Boolean

    /** When performing occlusion culling, this method is a callback for when a collision object is found to be visible from the
     * point of view of the camera.
     *
     *
     * When performing k-DOP culling, this method is a callback for when a collision object is found to be inside the camera
     * frustum (regardless of occlusion).
     *
     * @param object A collision object which is visible to the camera
     */
    abstract fun onObjectVisible(`object`: btCollisionObject)

    /** Performs k-DOP (k-Discrete Oriented Polytope) culling using the Bullet method [btDbvt.collideKDOP]. Finds all
     * collision objects inside the camera frustum, each of which will trigger a callback to
     * [.onObjectVisible]. The process of finding objects in frustum is accelerated by the broadphase
     * dynamic bounding volume tree ([btDbvt]).
     *
     * @param broadphase The dynamics world broadphase
     * @param camera Camera for which to perform k-DOP culling
     */
    fun performKDOPCulling(broadphase: btDbvtBroadphase?, camera: Camera?) {
        setFrustumPlanes(camera!!.frustum)
        btDbvt.collideKDOP(broadphase!!.set1.root, frustumNormals, frustumOffsets, NUM_PLANES, collider)
        btDbvt.collideKDOP(broadphase.set0.root, frustumNormals, frustumOffsets, NUM_PLANES, collider)
    }

    /** Performs occlusion culling using the Bullet method [btDbvt.collideOCL]. Finds all collision objects which are visible
     * to the camera, where vision is not blocked (occluded) by another object. If a collision object from the broadphase is
     * visible to the camera, a callback is made to [.onObjectVisible]. Only collision objects for which
     * [.isOccluder] returns true can occlude others. These collision objects must have a
     * [btBoxShape] collision shape. However, box occluders can block vision of any type of shape.
     *
     * @param broadphase The dynamics world broadphase
     * @param oclBuffer The occlusion buffer in which to query and draw depth during culling
     * @param camera Camera for which to perform occlusion culling
     */
    fun performOcclusionCulling(broadphase: btDbvtBroadphase?, oclBuffer: OcclusionBuffer?, camera: Camera?) {
        this.oclBuffer = oclBuffer
        oclBuffer!!.setProjectionMatrix(camera!!.combined)
        setFrustumPlanes(camera.frustum)
        btDbvt.collideOCL(
            broadphase!!.set1.root,
            frustumNormals,
            frustumOffsets,
            camera.direction,
            NUM_PLANES,
            collider
        )
        btDbvt.collideOCL(broadphase.set0.root, frustumNormals, frustumOffsets, camera.direction, NUM_PLANES, collider)
    }

    /** @param frustum Set the frustum plane buffers to this frustum
     */
    private fun setFrustumPlanes(frustum: Frustum) {
        // All frustum planes except 'near' (index 0) should be sent to Bullet.
        (frustumNormals as Buffer).clear()
        (frustumOffsets as Buffer).clear()
        for (i in 1..5) {
            val plane = frustum.planes[i]
            // Since the plane normals map to an array of btVector3, all four vector components (x, y, z, w)
            // required by the C++ struct must be provided. The plane offset from origin (d) must also be set.
            frustumNormals.put(plane.normal.x)
            frustumNormals.put(plane.normal.y)
            frustumNormals.put(plane.normal.z)
            frustumNormals.put(0f)
            frustumOffsets.put(plane.d)
        }
    }

    companion object {
        private const val NUM_PLANES = 5
    }
}