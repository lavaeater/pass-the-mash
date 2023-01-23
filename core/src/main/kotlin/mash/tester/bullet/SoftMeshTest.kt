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
//import com.badlogic.gdx.graphics.VertexAttributes
//import com.badlogic.gdx.graphics.g3d.Model
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3
//import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher
//import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
//import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration
//import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
//import com.badlogic.gdx.physics.bullet.softbody.btSoftBody
//import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyRigidBodyCollisionConfiguration
//import com.badlogic.gdx.physics.bullet.softbody.btSoftBodyWorldInfo
//import com.badlogic.gdx.physics.bullet.softbody.btSoftRigidDynamicsWorld
//import com.badlogic.gdx.utils.BufferUtils
//import java.nio.ShortBuffer
//
///** @author xoppa
// */
//class SoftMeshTest : BaseBulletTest() {
//    var worldInfo: btSoftBodyWorldInfo? = null
//    var softBody: btSoftBody? = null
//    var model: Model? = null
//    var entity: BulletEntity? = null
//    var indexMap: ShortBuffer? = null
//    var tmpV = Vector3()
//    var positionOffset = 0
//    var normalOffset = 0
//    override fun createWorld(): BulletWorld? {
//        val collisionConfiguration: btDefaultCollisionConfiguration = btSoftBodyRigidBodyCollisionConfiguration()
//        val dispatcher = btCollisionDispatcher(collisionConfiguration)
//        val broadphase = btAxisSweep3(
//            BaseBulletTest.Companion.tmpV1.set(-1000f, -1000f, -1000f),
//            BaseBulletTest.Companion.tmpV2.set(1000f, 1000f, 1000f),
//            1024
//        )
//        val solver = btSequentialImpulseConstraintSolver()
//        val dynamicsWorld = btSoftRigidDynamicsWorld(
//            dispatcher, broadphase, solver,
//            collisionConfiguration
//        )
//        worldInfo = btSoftBodyWorldInfo()
//        worldInfo!!.broadphase = broadphase
//        worldInfo!!.dispatcher = dispatcher
//        worldInfo!!.sparsesdf.Initialize()
//        return BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, dynamicsWorld)
//    }
//
//    override fun create() {
//        super.create()
//        world!!.maxSubSteps = 20
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//
//        // Note: not every model is suitable for a one on one translation with a soft body, a better model might be added later.
//        model = objLoader.loadModel(Gdx.files.internal("data/wheel.obj"))
//        val meshPart = model.nodes[0].parts[0].meshPart
//        meshPart.mesh.scale(6f, 6f, 6f)
//        indexMap = BufferUtils.newShortBuffer(meshPart.size)
//        positionOffset = meshPart.mesh.getVertexAttribute(VertexAttributes.Usage.Position).offset
//        normalOffset = meshPart.mesh.getVertexAttribute(VertexAttributes.Usage.Normal).offset
//        softBody = btSoftBody(
//            worldInfo, meshPart.mesh.getVerticesBuffer(false), meshPart.mesh.vertexSize, positionOffset,
//            normalOffset, meshPart.mesh.getIndicesBuffer(false), meshPart.offset, meshPart.size, indexMap, 0
//        )
//        // Set mass of the first vertex to zero so its unmovable, comment out this line to make it a fully dynamic body.
//        softBody!!.setMass(0, 0f)
//        val pm = softBody!!.appendMaterial()
//        pm.klst = 0.2f
//        pm.flags = 0
//        softBody!!.generateBendingConstraints(2, pm)
//        // Be careful increasing iterations, it decreases performance (but increases accuracy).
//        softBody!!.setConfig_piterations(7)
//        softBody!!.setConfig_kDF(0.2f)
//        softBody!!.randomizeConstraints()
//        softBody!!.totalMass = 1f
//        softBody!!.translate(tmpV.set(1f, 5f, 1f))
//        (world!!.collisionWorld as btSoftRigidDynamicsWorld).addSoftBody(softBody)
//        world!!.add(BulletEntity(model, null as btCollisionObject?, 1f, 5f, 1f).also { entity = it })
//    }
//
//    override fun dispose() {
//        (world!!.collisionWorld as btSoftRigidDynamicsWorld).removeSoftBody(softBody)
//        softBody!!.dispose()
//        softBody = null
//        indexMap = null
//        super.dispose()
//        worldInfo!!.dispose()
//        worldInfo = null
//        model!!.dispose()
//        model = null
//    }
//
//    override fun render() {
//        if (world!!.renderMeshes) {
//            val meshPart = model!!.nodes[0].parts[0].meshPart
//            softBody!!.getVertices(
//                meshPart.mesh.getVerticesBuffer(true), meshPart.mesh.vertexSize, positionOffset, normalOffset,
//                meshPart.mesh.getIndicesBuffer(false), meshPart.offset, meshPart.size, indexMap, 0
//            )
//            softBody!!.getWorldTransform(entity!!.transform)
//        }
//        super.render()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y, 20f)
//        return true
//    }
//}