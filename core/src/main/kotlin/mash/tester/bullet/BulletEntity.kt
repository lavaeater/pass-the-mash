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
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState

/** @author xoppa Renderable BaseEntity with a bullet physics body.
 */
class BulletEntity(modelInstance: ModelInstance, body: btCollisionObject?) : BaseEntity() {
    lateinit var motionState: MotionState
    var body: btCollisionObject? = null
    val boundingBox = BoundingBox()
    val boundingBoxRadius: Float

    constructor(
        model: Model, bodyInfo: btRigidBodyConstructionInfo?, x: Float, y: Float,
        z: Float
    ) : this(model, bodyInfo?.let { btRigidBody(it) }, x, y, z)

    constructor(model: Model, bodyInfo: btRigidBodyConstructionInfo?, transform: Matrix4) : this(
        model,
        bodyInfo.let { btRigidBody(it) },
        transform
    )

    constructor(model: Model, body: btCollisionObject?, x: Float, y: Float, z: Float) : this(
        model,
        body,
        tmpM.setToTranslation(x, y, z)
    )

    constructor(model: Model, body: btCollisionObject?, transform: Matrix4) : this(
        ModelInstance(
            model,
            transform.cpy()
        ), body
    )

    init {
        this.modelInstance = modelInstance
        transform = this.modelInstance!!.transform
        this.body = body
        modelInstance.calculateBoundingBox(boundingBox)
        boundingBoxRadius = boundingBox.getDimensions(Vector3()).len() * 0.5f
        if (body != null) {
            body.userData = this
            if (body is btRigidBody) {
                motionState = MotionState(this.modelInstance!!.transform)
                (this.body as btRigidBody).motionState = motionState
            } else body.worldTransform = transform
        }
    }

    override fun dispose() {
        // Don't rely on the GC
        motionState.dispose()
        if (body != null) body!!.dispose()
        // And remove the reference
    }

    class MotionState(private val transform: Matrix4) : btMotionState() {
        /** For dynamic and static bodies this method is called by bullet once to get the initial state of the body. For kinematic
         * bodies this method is called on every update, unless the body is deactivated.  */
        override fun getWorldTransform(worldTrans: Matrix4) {
            worldTrans.set(transform)
        }

        /** For dynamic bodies this method is called by bullet every update to inform about the new position and rotation.  */
        override fun setWorldTransform(worldTrans: Matrix4) {
            transform.set(worldTrans)
        }
    }

    companion object {
        private val tmpM = Matrix4()
    }
}