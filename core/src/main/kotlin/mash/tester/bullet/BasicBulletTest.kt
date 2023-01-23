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
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import com.badlogic.gdx.utils.Array

/** @author xoppa
 */
class BasicBulletTest : BulletTest() {
    var modelBatch: ModelBatch? = null
    var lights: Environment? = null
    var modelBuilder = ModelBuilder()
    lateinit var collisionConfiguration: btCollisionConfiguration
    lateinit var dispatcher: btCollisionDispatcher
    lateinit var broadphase: btBroadphaseInterface
    lateinit var solver: btConstraintSolver
    lateinit var collisionWorld: btDynamicsWorld
    var gravity = Vector3(0f, -9.81f, 0f)
    var tempVector = Vector3()
    var models = Array<Model>()
    var instances = Array<ModelInstance>()
    var motionStates = Array<btDefaultMotionState>()
    var bodyInfos = Array<btRigidBodyConstructionInfo>()
    var shapes = Array<btCollisionShape>()
    var bodies = Array<btRigidBody>()
    override fun create() {
        super.create()
        instructions = "Swipe for next test"
        lights = Environment()
        lights!!.set(ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f))
        lights!!.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.7f))

        // Set up the camera
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        camera = if (width > height) PerspectiveCamera(67f, 3f * width / height, 3f) else PerspectiveCamera(
            67f,
            3f,
            3f * height / width
        )
        camera!!.position[10f, 10f] = 10f
        camera!!.lookAt(0f, 0f, 0f)
        camera!!.update()
        // Create the model batch
        modelBatch = ModelBatch()
        // Create some basic models
        val groundModel = modelBuilder.createRect(
            20f, 0f, -20f, -20f, 0f, -20f, -20f, 0f, 20f, 20f, 0f, 20f, 0f, 1f, 0f,
            Material(
                ColorAttribute.createDiffuse(Color.BLUE), ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(16f)
            ),
            (
                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        )
        models.add(groundModel)
        val sphereModel = modelBuilder.createSphere(
            1f, 1f, 1f, 10, 10,
            Material(
                ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(64f)
            ),
            (
                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        )
        models.add(sphereModel)
        // Load the bullet library
        BaseBulletTest.Companion.init() // Normally use: Bullet.init();
        // Create the bullet world
        collisionConfiguration = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfiguration)
        broadphase = btDbvtBroadphase()
        solver = btSequentialImpulseConstraintSolver()
        collisionWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration)
        collisionWorld.setGravity(gravity)
        // Create the shapes and body construction infos
        val groundShape: btCollisionShape = btBoxShape(tempVector.set(20f, 0f, 20f))
        shapes.add(groundShape)
        val groundInfo = btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero)
        bodyInfos.add(groundInfo)
        val sphereShape: btCollisionShape = btSphereShape(0.5f)
        shapes.add(sphereShape)
        sphereShape.calculateLocalInertia(1f, tempVector)
        val sphereInfo = btRigidBodyConstructionInfo(1f, null, sphereShape, tempVector)
        bodyInfos.add(sphereInfo)
        // Create the ground
        val ground = ModelInstance(groundModel)
        instances.add(ground)
        val groundMotionState = btDefaultMotionState()
        groundMotionState.setWorldTransform(ground.transform)
        motionStates.add(groundMotionState)
        val groundBody = btRigidBody(groundInfo)
        groundBody.motionState = groundMotionState
        bodies.add(groundBody)
        collisionWorld.addRigidBody(groundBody)
        // Create the spheres
        var x = -10f
        while (x <= 10f) {
            var y = 5f
            while (y <= 15f) {
                var z = 0f
                while (z <= 0f) {
                    val sphere = ModelInstance(sphereModel)
                    instances.add(sphere)
                    sphere.transform.trn(
                        x + 0.1f * MathUtils.random(),
                        y + 0.1f * MathUtils.random(),
                        z + 0.1f * MathUtils.random()
                    )
                    val sphereMotionState = btDefaultMotionState()
                    sphereMotionState.setWorldTransform(sphere.transform)
                    motionStates.add(sphereMotionState)
                    val sphereBody = btRigidBody(sphereInfo)
                    sphereBody.motionState = sphereMotionState
                    bodies.add(sphereBody)
                    collisionWorld.addRigidBody(sphereBody)
                    z += 2f
                }
                y += 2f
            }
            x += 2f
        }
    }

    override fun render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        fpsCounter.put(Gdx.graphics.framesPerSecond.toFloat())
        performanceCounter.tick()
        performanceCounter.start()
        collisionWorld!!.stepSimulation(Gdx.graphics.deltaTime, 5)
        performanceCounter.stop()
        val c = motionStates.size
        for (i in 0 until c) {
            motionStates[i].getWorldTransform(instances[i].transform)
        }
        modelBatch!!.begin(camera)
        modelBatch!!.render(instances, lights)
        modelBatch!!.end()
        performance.setLength(0)
        performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
            .append((performanceCounter.load.value * 100f).toInt()).append("%")
    }

    override fun dispose() {
        collisionWorld!!.dispose()
        solver!!.dispose()
        broadphase!!.dispose()
        dispatcher!!.dispose()
        collisionConfiguration!!.dispose()
        for (body in bodies) body.dispose()
        bodies.clear()
        for (motionState in motionStates) motionState.dispose()
        motionStates.clear()
        for (shape in shapes) shape.dispose()
        shapes.clear()
        for (info in bodyInfos) info.dispose()
        bodyInfos.clear()
        modelBatch!!.dispose()
        instances.clear()
        for (model in models) model.dispose()
        models.clear()
    }
}