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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.physics.bullet.collision.Collision
import com.badlogic.gdx.physics.bullet.collision.btBoxShape
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle.btVehicleTuning

/** @author Xoppa
 */
open class VehicleTest : BaseBulletTest() {
    lateinit var _raycaster: btVehicleRaycaster
    lateinit     var vehicle: btRaycastVehicle
    lateinit var tuning: btVehicleTuning
    lateinit var chassis: BulletEntity
    var wheels = arrayOfNulls<BulletEntity>(4)
    var downPressed = false
    var upPressed = false
    var leftPressed = false
    var rightPressed = false
    var tmpV = Vector3()
    protected open fun getRaycaster(): btVehicleRaycaster {
        return btDefaultVehicleRaycaster(world.collisionWorld as btDynamicsWorld)
    }

    override fun create() {
        super.create()
        Gdx.input.inputProcessor = this
        instructions =
            "Tap to shoot\nArrow keys to drive\nR to reset\nLong press to toggle debug mode\nSwipe for next test"
        val chassisModel = objLoader.loadModel(Gdx.files.internal("data/car.obj"))
        disposables.add(chassisModel)
        chassisModel.materials[0].clear()
        chassisModel.materials[0][ColorAttribute.createDiffuse(Color.RED)] =
            ColorAttribute.createSpecular(Color.WHITE)
        val wheelModel = objLoader.loadModel(Gdx.files.internal("data/wheel.obj"))
        disposables.add(wheelModel)
        wheelModel.materials[0].clear()
        wheelModel.materials[0][ColorAttribute.createDiffuse(Color.BLACK), ColorAttribute.createSpecular(Color.WHITE)] =
            FloatAttribute.createShininess(128f)
        val checkboard = Texture(Gdx.files.internal("data/g3d/checkboard.png"))
        val largeGroundModel = modelBuilder.createBox(
            200f, 2f, 200f,
            Material(
                TextureAttribute.createDiffuse(checkboard), ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(16f)
            ),
            (
                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong()
        )
        largeGroundModel.manageDisposable(checkboard)
        disposables.add(largeGroundModel)
        world.addConstructor("largeground", BulletConstructor(largeGroundModel, 0f))
        val bounds = BoundingBox()
        val chassisHalfExtents = chassisModel.calculateBoundingBox(bounds).getDimensions(Vector3()).scl(0.5f)
        val wheelHalfExtents = wheelModel.calculateBoundingBox(bounds).getDimensions(Vector3()).scl(0.5f)
        world.addConstructor("chassis", BulletConstructor(chassisModel, 5f, btBoxShape(chassisHalfExtents)))
        world.addConstructor("wheel", BulletConstructor(wheelModel, 0f, null))
        var i = 0
        while (i < 1000) {
            world.add("largeground", 0f, -1f, i.toFloat())
            i += 200
        }
        chassis = world.add("chassis", 0f, 3f, 0f)
        wheels[0] = world.add("wheel", 0f, 0f, 0f)
        wheels[1] = world.add("wheel", 0f, 0f, 0f)
        wheels[2] = world.add("wheel", 0f, 0f, 0f)
        wheels[3] = world.add("wheel", 0f, 0f, 0f)

        // Create the vehicle
        _raycaster = getRaycaster()
        tuning = btVehicleTuning()
        vehicle = btRaycastVehicle(tuning, chassis.body as btRigidBody, _raycaster)
        chassis!!.body!!.activationState = Collision.DISABLE_DEACTIVATION
        (world.collisionWorld as btDynamicsWorld).addVehicle(vehicle)
        vehicle.setCoordinateSystem(0, 1, 2)
        var wheelInfo: btWheelInfo?
        val point = Vector3()
        val direction = Vector3(0f, -1f, 0f)
        val axis = Vector3(-1f, 0f, 0f)
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(0.9f, -0.8f, 0.7f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, true
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(-0.9f, -0.8f, 0.7f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, true
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(0.9f, -0.8f, -0.5f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, false
        )
        wheelInfo = vehicle.addWheel(
            point.set(chassisHalfExtents).scl(-0.9f, -0.8f, -0.5f), direction, axis,
            wheelHalfExtents.z * 0.3f, wheelHalfExtents.z, tuning, false
        )
    }

    var maxForce = 100f
    var currentForce = 0f
    var acceleration = 50f // force/second
    var maxAngle = 60f
    var currentAngle = 0f
    var steerSpeed = 45f // angle/second
    override fun update() {
        val delta = Gdx.graphics.deltaTime
        var angle = currentAngle
        if (rightPressed) {
            if (angle > 0f) angle = 0f
            angle = MathUtils.clamp(angle - steerSpeed * delta, -maxAngle, 0f)
        } else if (leftPressed) {
            if (angle < 0f) angle = 0f
            angle = MathUtils.clamp(angle + steerSpeed * delta, 0f, maxAngle)
        } else angle = 0f
        if (angle != currentAngle) {
            currentAngle = angle
            vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 0)
            vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 1)
        }
        var force = currentForce
        if (upPressed) {
            if (force < 0f) force = 0f
            force = MathUtils.clamp(force + acceleration * delta, 0f, maxForce)
        } else if (downPressed) {
            if (force > 0f) force = 0f
            force = MathUtils.clamp(force - acceleration * delta, -maxForce, 0f)
        } else force = 0f
        if (force != currentForce) {
            currentForce = force
            vehicle.applyEngineForce(force, 0)
            vehicle.applyEngineForce(force, 1)
        }
        super.update()
        for (i in wheels.indices) {
            vehicle.updateWheelTransform(i, true)
            vehicle.getWheelInfo(i).worldTransform.getOpenGLMatrix(wheels[i]!!.transform!!.`val`)
        }
        chassis.transform!!.getTranslation(camera.position)
        tmpV.set(camera.position).sub(5f, 0f, 5f).y = 0f
        camera.position.add(tmpV.nor().scl(-6f)).y = 4f
        chassis.transform!!.getTranslation(tmpV)
        camera.lookAt(tmpV)
        camera.up.set(Vector3.Y)
        camera.update()
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }

    override fun dispose() {
        super.dispose()
        vehicle.dispose()
        vehicle
        _raycaster.dispose()
        _raycaster
        tuning.dispose()
        tuning
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.DOWN -> downPressed = true
            Input.Keys.UP -> upPressed = true
            Input.Keys.LEFT -> leftPressed = true
            Input.Keys.RIGHT -> rightPressed = true
        }
        return super.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.DOWN -> downPressed = false
            Input.Keys.UP -> upPressed = false
            Input.Keys.LEFT -> leftPressed = false
            Input.Keys.RIGHT -> rightPressed = false
            Input.Keys.R -> {
                chassis.body!!.worldTransform = chassis.transform!!.setToTranslation(0f, 5f, 0f)
                chassis.body!!.interpolationWorldTransform = chassis.transform
                (chassis.body as btRigidBody).linearVelocity = Vector3.Zero
                (chassis.body as btRigidBody).angularVelocity = Vector3.Zero
                chassis!!.body!!.activate()
            }
        }
        return super.keyUp(keycode)
    }
}