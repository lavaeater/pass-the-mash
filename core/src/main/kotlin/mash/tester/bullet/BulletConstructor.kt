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

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo

/** @author xoppa Holds the information necessary to create a bullet btRigidBody. This class should outlive the btRigidBody
 * (entity) itself.
 */
class BulletConstructor : BaseWorld.Constructor<BulletEntity> {
    var bodyInfo: btRigidBodyConstructionInfo? = null
    var shape: btCollisionShape? = null

    /** Specify null for the shape to use only the renderable part of this entity and not the physics part.  */
    constructor(model: Model, mass: Float, shape: btCollisionShape?) {
        create(model, mass, shape)
    }

    /** Specify null for the shape to use only the renderable part of this entity and not the physics part.  */
    constructor(model: Model, shape: btCollisionShape?) : this(model, -1f, shape)

    /** Creates a btBoxShape with the specified dimensions.  */
    constructor(model: Model, mass: Float, width: Float, height: Float, depth: Float) {
        create(model, mass, width, height, depth)
    }

    /** Creates a btBoxShape with the specified dimensions and NO rigidbody.  */
    constructor(model: Model, width: Float, height: Float, depth: Float) : this(model, -1f, width, height, depth)
    /** Creates a btBoxShape with the same dimensions as the shape.  */
    /** Creates a btBoxShape with the same dimensions as the shape and NO rigidbody.  */
    @JvmOverloads
    constructor(model: Model, mass: Float = -1f) {
        val boundingBox = BoundingBox()
        model.calculateBoundingBox(boundingBox)
        create(model, mass, boundingBox.width, boundingBox.height, boundingBox.depth)
    }

    private fun create(model: Model, mass: Float, width: Float, height: Float, depth: Float) {
        // Create a simple boxshape
        create(model, mass, btBoxShape(tmpV.set(width * 0.5f, height * 0.5f, depth * 0.5f)))
    }

    private fun create(model: Model, mass: Float, shape: btCollisionShape?) {
        this.model = model
        this.shape = shape
        if (shape != null && mass >= 0) {
            // Calculate the local inertia, bodies with no mass are static
            val localInertia: Vector3
            localInertia = if (mass == 0f) Vector3.Zero else {
                shape.calculateLocalInertia(mass, tmpV)
                tmpV
            }

            // For now just pass null as the motionstate, we'll add that to the body in the entity itself
            bodyInfo = btRigidBodyConstructionInfo(mass, null, shape, localInertia)
        }
    }

    override fun dispose() {
        // Don't rely on the GC
        bodyInfo?.dispose()
        shape!!.dispose()
    }

    override fun construct(x: Float, y: Float, z: Float): BulletEntity {
        return if (bodyInfo == null && shape != null) {
            val obj = btCollisionObject()
            obj.collisionShape = shape
            BulletEntity(model, obj, x, y, z)
        } else BulletEntity(model, bodyInfo, x, y, z)
    }

    override fun construct(transform: Matrix4): BulletEntity {
        return if (bodyInfo == null && shape != null) {
            val obj = btCollisionObject()
            obj.collisionShape = shape
            BulletEntity(model, obj, transform)
        } else BulletEntity(model, bodyInfo!!, transform)
    }

    companion object {
        private val tmpV = Vector3()
    }
}