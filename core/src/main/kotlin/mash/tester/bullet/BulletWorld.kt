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
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes
import com.badlogic.gdx.utils.PerformanceCounter

/** @author xoppa Bullet physics world that holds all bullet entities and constructors.
 */
open class BulletWorld : BaseWorld<BulletEntity> {
    var debugDrawer: DebugDrawer? = null
    var renderMeshes = true
    val collisionConfiguration: btCollisionConfiguration?
    val dispatcher: btCollisionDispatcher?
    val broadphase: btBroadphaseInterface?
    val solver: btConstraintSolver?
    val collisionWorld: btCollisionWorld
    var performanceCounter: PerformanceCounter? = null
    val gravity: Vector3
    var maxSubSteps = 5
    var fixedTimeStep = 1f / 60f

    @JvmOverloads
    constructor(
        collisionConfiguration: btCollisionConfiguration?, dispatcher: btCollisionDispatcher?,
        broadphase: btBroadphaseInterface?, solver: btConstraintSolver?, world: btCollisionWorld,
        gravity: Vector3 = Vector3(0f, -10f, 0f)
    ) {
        this.collisionConfiguration = collisionConfiguration
        this.dispatcher = dispatcher
        this.broadphase = broadphase
        this.solver = solver
        collisionWorld = world
        if (world is btDynamicsWorld) (collisionWorld as btDynamicsWorld).gravity = gravity
        this.gravity = gravity
    }

    @JvmOverloads
    constructor(gravity: Vector3 = Vector3(0f, -10f, 0f)) {
        collisionConfiguration = btDefaultCollisionConfiguration()
        dispatcher = btCollisionDispatcher(collisionConfiguration)
        broadphase = btDbvtBroadphase()
        solver = btSequentialImpulseConstraintSolver()
        collisionWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration)
        (collisionWorld as btDynamicsWorld).gravity = gravity
        this.gravity = gravity
    }

    override fun add(entity: BulletEntity) {
        super.add(entity)
        if (entity!!.body != null) {
            if (entity.body is btRigidBody) (collisionWorld as btDiscreteDynamicsWorld).addRigidBody(entity.body as btRigidBody) else collisionWorld.addCollisionObject(
                entity.body
            )
            // Store the index of the entity in the collision object.
            entity.body!!.userValue = entities.size - 1
        }
    }

    override fun update() {
        if (performanceCounter != null) {
            performanceCounter!!.tick()
            performanceCounter!!.start()
        }
        if (collisionWorld is btDynamicsWorld) collisionWorld.stepSimulation(
            Gdx.graphics.deltaTime,
            maxSubSteps,
            fixedTimeStep
        )
        if (performanceCounter != null) performanceCounter!!.stop()
    }

    override fun render(batch: ModelBatch, lights: Environment?, entities: Iterable<BulletEntity>) {
        if (renderMeshes) super.render(batch, lights, entities)
        if (debugDrawer != null && debugDrawer!!.debugMode > 0) {
            batch.flush()
            debugDrawer!!.begin(batch.camera)
            collisionWorld.debugDrawWorld()
            debugDrawer!!.end()
        }
    }

    override fun dispose() {
        for (i in 0 until entities.size) {
            val body = entities[i]!!.body
            if (body != null) {
                if (body is btRigidBody) (collisionWorld as btDynamicsWorld).removeRigidBody(body) else collisionWorld.removeCollisionObject(
                    body
                )
            }
        }
        super.dispose()
        collisionWorld.dispose()
        solver?.dispose()
        broadphase?.dispose()
        dispatcher?.dispose()
        collisionConfiguration?.dispose()
    }

    var debugMode: Int
        get() = if (debugDrawer == null) 0 else debugDrawer!!.debugMode
        set(mode) {
            if (mode == DebugDrawModes.DBG_NoDebug && debugDrawer == null) return
            if (debugDrawer == null) collisionWorld.debugDrawer = DebugDrawer().also { debugDrawer = it }
            debugDrawer!!.debugMode = mode
        }
}