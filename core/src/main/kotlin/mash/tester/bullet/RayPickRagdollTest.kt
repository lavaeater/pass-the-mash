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
//import com.badlogic.gdx.Input.Buttons
//import com.badlogic.gdx.graphics.*
//import com.badlogic.gdx.graphics.g3d.*
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.math.*
//import com.badlogic.gdx.physics.bullet.collision.*
//import com.badlogic.gdx.physics.bullet.dynamics.*
//import com.badlogic.gdx.utils.Array
//
///** @author xoppa
// */
//class RayPickRagdollTest : BaseBulletTest() {
//    val constraints = Array<btTypedConstraint>()
//    var pickConstraint: btPoint2PointConstraint? = null
//    var pickedBody: btRigidBody? = null
//    var pickDistance = 0f
//    var tmpV = Vector3()
//    override fun create() {
//        super.create()
//        instructions =
//            "Tap to shoot\nDrag ragdoll to pick\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
//        camera!!.position[4f, 2f] = 4f
//        camera!!.lookAt(0f, 1f, 0f)
//        camera!!.update()
//        world!!.addConstructor(
//            "pelvis",
//            BulletConstructor(createCapsuleModel(0.15f, 0.2f), 1f, btCapsuleShape(0.15f, 0.2f))
//        )
//        world!!.addConstructor(
//            "spine",
//            BulletConstructor(createCapsuleModel(0.15f, 0.28f), 1f, btCapsuleShape(0.15f, 0.28f))
//        )
//        world!!.addConstructor(
//            "head",
//            BulletConstructor(createCapsuleModel(0.1f, 0.05f), 1f, btCapsuleShape(0.1f, 0.05f))
//        )
//        world!!.addConstructor(
//            "upperleg",
//            BulletConstructor(createCapsuleModel(0.07f, 0.45f), 1f, btCapsuleShape(0.07f, 0.45f))
//        )
//        world!!.addConstructor(
//            "lowerleg",
//            BulletConstructor(createCapsuleModel(0.05f, 0.37f), 1f, btCapsuleShape(0.05f, 0.37f))
//        )
//        world!!.addConstructor(
//            "upperarm",
//            BulletConstructor(createCapsuleModel(0.05f, 0.33f), 1f, btCapsuleShape(0.05f, 0.33f))
//        )
//        world!!.addConstructor(
//            "lowerarm",
//            BulletConstructor(createCapsuleModel(0.04f, 0.25f), 1f, btCapsuleShape(0.04f, 0.25f))
//        )
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        addRagdoll(0f, 3f, 0f)
//        addRagdoll(1f, 6f, 0f)
//        addRagdoll(-1f, 12f, 0f)
//    }
//
//    override fun dispose() {
//        for (i in 0 until constraints.size) {
//            (world!!.collisionWorld as btDynamicsWorld).removeConstraint(constraints[i])
//            constraints[i].dispose()
//        }
//        constraints.clear()
//        super.dispose()
//    }
//
//    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//        var result = false
//        if (button == Buttons.LEFT) {
//            val ray = camera!!.getPickRay(screenX.toFloat(), screenY.toFloat())
//            BaseBulletTest.Companion.tmpV1.set(ray.direction).scl(10f).add(ray.origin)
//            val cb = ClosestRayResultCallback(ray.origin, BaseBulletTest.Companion.tmpV1)
//            world!!.collisionWorld.rayTest(ray.origin, BaseBulletTest.Companion.tmpV1, cb)
//            if (cb.hasHit()) {
//                val body = cb.collisionObject as btRigidBody
//                if (body != null && !body.isStaticObject && !body.isKinematicObject) {
//                    pickedBody = body
//                    body.activationState = Collision.DISABLE_DEACTIVATION
//                    cb.getHitPointWorld(tmpV)
//                    tmpV.mul(body.centerOfMassTransform.inv())
//                    pickConstraint = btPoint2PointConstraint(body, tmpV)
//                    val setting = pickConstraint!!.setting
//                    setting.impulseClamp = 30f
//                    setting.tau = 0.001f
//                    pickConstraint!!.setting = setting
//                    (world!!.collisionWorld as btDynamicsWorld).addConstraint(pickConstraint)
//                    pickDistance = BaseBulletTest.Companion.tmpV1.sub(camera!!.position).len()
//                    result = true
//                }
//            }
//            cb.dispose()
//        }
//        return if (result) result else super.touchDown(screenX, screenY, pointer, button)
//    }
//
//    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//        var result = false
//        if (button == Buttons.LEFT) {
//            if (pickConstraint != null) {
//                (world!!.collisionWorld as btDynamicsWorld).removeConstraint(pickConstraint)
//                pickConstraint!!.dispose()
//                pickConstraint = null
//                result = true
//            }
//            if (pickedBody != null) {
//                pickedBody!!.forceActivationState(Collision.ACTIVE_TAG)
//                pickedBody!!.deactivationTime = 0f
//                pickedBody = null
//            }
//        }
//        return if (result) result else super.touchUp(screenX, screenY, pointer, button)
//    }
//
//    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
//        var result = false
//        if (pickConstraint != null) {
//            val ray = camera!!.getPickRay(screenX.toFloat(), screenY.toFloat())
//            BaseBulletTest.Companion.tmpV1.set(ray.direction).scl(pickDistance).add(camera!!.position)
//            pickConstraint!!.setPivotB(BaseBulletTest.Companion.tmpV1)
//            result = true
//        }
//        return if (result) result else super.touchDragged(screenX, screenY, pointer)
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//
//    fun addRagdoll(x: Float, y: Float, z: Float) {
//        val tmpM = Matrix4()
//        val pelvis = world!!.add("pelvis", x, y + 1, z)!!.body as btRigidBody
//        val spine = world!!.add("spine", x, y + 1.2f, z)!!.body as btRigidBody
//        val head = world!!.add("head", x, y + 1.6f, z)!!.body as btRigidBody
//        val leftupperleg = world!!.add("upperleg", x - 0.18f, y + 0.65f, z)!!.body as btRigidBody
//        val leftlowerleg = world!!.add("lowerleg", x - 0.18f, y + 0.2f, z)!!.body as btRigidBody
//        val rightupperleg = world!!.add("upperleg", x + 0.18f, y + 0.65f, z)!!.body as btRigidBody
//        val rightlowerleg = world!!.add("lowerleg", x + 0.18f, y + 0.2f, z)!!.body as btRigidBody
//        val leftupperarm = world!!.add(
//            "upperarm",
//            tmpM.setFromEulerAnglesRad(PI2, 0f, 0f).trn(x - 0.35f, y + 1.45f, z)
//        )!!.body as btRigidBody
//        val leftlowerarm = world!!.add(
//            "lowerarm",
//            tmpM.setFromEulerAnglesRad(PI2, 0f, 0f).trn(x - 0.7f, y + 1.45f, z)
//        )!!.body as btRigidBody
//        val rightupperarm = world!!.add(
//            "upperarm",
//            tmpM.setFromEulerAnglesRad(-PI2, 0f, 0f).trn(x + 0.35f, y + 1.45f, z)
//        )!!.body as btRigidBody
//        val rightlowerarm = world!!.add(
//            "lowerarm",
//            tmpM.setFromEulerAnglesRad(-PI2, 0f, 0f).trn(x + 0.7f, y + 1.45f, z)
//        )!!.body as btRigidBody
//        val localA = Matrix4()
//        val localB = Matrix4()
//        var hingeC: btHingeConstraint? = null
//        var coneC: btConeTwistConstraint? = null
//
//        // PelvisSpine
//        localA.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, 0.15f, 0f)
//        localB.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, -0.15f, 0f)
//        constraints.add(btHingeConstraint(pelvis, spine, localA, localB).also { hingeC = it })
//        hingeC!!.setLimit(-PI4, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(hingeC, true)
//
//        // SpineHead
//        localA.setFromEulerAnglesRad(PI2, 0f, 0f).trn(0f, 0.3f, 0f)
//        localB.setFromEulerAnglesRad(PI2, 0f, 0f).trn(0f, -0.14f, 0f)
//        constraints.add(btConeTwistConstraint(spine, head, localA, localB).also { coneC = it })
//        coneC!!.setLimit(PI4, PI4, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(coneC, true)
//
//        // LeftHip
//        localA.setFromEulerAnglesRad(-PI4 * 5f, 0f, 0f).trn(-0.18f, -0.1f, 0f)
//        localB.setFromEulerAnglesRad(-PI4 * 5f, 0f, 0f).trn(0f, 0.225f, 0f)
//        constraints.add(btConeTwistConstraint(pelvis, leftupperleg, localA, localB).also { coneC = it })
//        coneC!!.setLimit(PI4, PI4, 0f)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(coneC, true)
//
//        // LeftKnee
//        localA.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, -0.225f, 0f)
//        localB.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, 0.185f, 0f)
//        constraints.add(btHingeConstraint(leftupperleg, leftlowerleg, localA, localB).also { hingeC = it })
//        hingeC!!.setLimit(0f, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(hingeC, true)
//
//        // RightHip
//        localA.setFromEulerAnglesRad(-PI4 * 5f, 0f, 0f).trn(0.18f, -0.1f, 0f)
//        localB.setFromEulerAnglesRad(-PI4 * 5f, 0f, 0f).trn(0f, 0.225f, 0f)
//        constraints.add(btConeTwistConstraint(pelvis, rightupperleg, localA, localB).also { coneC = it })
//        coneC!!.setLimit(PI4, PI4, 0f)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(coneC, true)
//
//        // RightKnee
//        localA.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, -0.225f, 0f)
//        localB.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, 0.185f, 0f)
//        constraints.add(btHingeConstraint(rightupperleg, rightlowerleg, localA, localB).also { hingeC = it })
//        hingeC!!.setLimit(0f, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(hingeC, true)
//
//        // LeftShoulder
//        localA.setFromEulerAnglesRad(PI, 0f, 0f).trn(-0.2f, 0.15f, 0f)
//        localB.setFromEulerAnglesRad(PI2, 0f, 0f).trn(0f, -0.18f, 0f)
//        constraints.add(btConeTwistConstraint(pelvis, leftupperarm, localA, localB).also { coneC = it })
//        coneC!!.setLimit(PI2, PI2, 0f)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(coneC, true)
//
//        // LeftElbow
//        localA.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, 0.18f, 0f)
//        localB.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, -0.14f, 0f)
//        constraints.add(btHingeConstraint(leftupperarm, leftlowerarm, localA, localB).also { hingeC = it })
//        hingeC!!.setLimit(0f, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(hingeC, true)
//
//        // RightShoulder
//        localA.setFromEulerAnglesRad(PI, 0f, 0f).trn(0.2f, 0.15f, 0f)
//        localB.setFromEulerAnglesRad(PI2, 0f, 0f).trn(0f, -0.18f, 0f)
//        constraints.add(btConeTwistConstraint(pelvis, rightupperarm, localA, localB).also { coneC = it })
//        coneC!!.setLimit(PI2, PI2, 0f)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(coneC, true)
//
//        // RightElbow
//        localA.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, 0.18f, 0f)
//        localB.setFromEulerAnglesRad(0f, PI2, 0f).trn(0f, -0.14f, 0f)
//        constraints.add(btHingeConstraint(rightupperarm, rightlowerarm, localA, localB).also { hingeC = it })
//        hingeC!!.setLimit(0f, PI2)
//        (world!!.collisionWorld as btDynamicsWorld).addConstraint(hingeC, true)
//    }
//
//    protected fun createCapsuleModel(radius: Float, height: Float): Model {
//        val result = modelBuilder.createCapsule(
//            radius, height + radius * 2f, 16,
//            Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)),
//            (
//                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )
//        disposables.add(result)
//        return result
//    }
//
//    companion object {
//        const val PI = MathUtils.PI
//        const val PI2 = 0.5f * PI
//        const val PI4 = 0.25f * PI
//    }
//}