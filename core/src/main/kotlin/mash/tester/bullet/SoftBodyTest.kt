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
//import com.badlogic.gdx.graphics.*
//import com.badlogic.gdx.graphics.g3d.*
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
//import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
//import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
//import com.badlogic.gdx.graphics.g3d.model.MeshPart
//import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
//import com.badlogic.gdx.graphics.glutils.ShaderProgram
//import com.badlogic.gdx.math.*
//import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3
//import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher
//import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration
//import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
//import com.badlogic.gdx.physics.bullet.softbody.*
//import java.nio.Buffer
//
///** @author xoppa
// */
//class SoftBodyTest : BaseBulletTest() {
//    var worldInfo: btSoftBodyWorldInfo? = null
//    var softBody: btSoftBody? = null
//    var texture: Texture? = null
//    var mesh: Mesh? = null
//    var model: Model? = null
//    var instance: ModelInstance? = null
//    var tmpM = Matrix4()
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
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        val x0 = -2f
//        val y0 = 6f
//        val z0 = -2f
//        val x1 = 8f
//        val y1 = 6f
//        val z1 = 8f
//        val patch00 = Vector3(x0, y0, z0)
//        val patch10 = Vector3(x1, y1, z0)
//        val patch01 = Vector3(x0, y0, z1)
//        val patch11 = Vector3(x1, y1, z1)
//        softBody = btSoftBodyHelpers.CreatePatch(worldInfo, patch00, patch10, patch01, patch11, 15, 15, 15, false)
//        softBody.takeOwnership()
//        softBody.setTotalMass(100f)
//        (world!!.collisionWorld as btSoftRigidDynamicsWorld).addSoftBody(softBody)
//        val vertCount = softBody.getNodeCount()
//        val faceCount = softBody.getFaceCount()
//        mesh = Mesh(
//            false,
//            vertCount,
//            faceCount * 3,
//            VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
//            VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
//            VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
//        )
//        val vertSize = mesh!!.vertexSize / 4
//        val verticesBuffer = mesh!!.getVerticesBuffer(true)
//        (verticesBuffer as Buffer).position(0)
//        (verticesBuffer as Buffer).limit(vertCount * vertSize)
//        val indicesBuffer = mesh!!.getIndicesBuffer(true)
//        (indicesBuffer as Buffer).position(0)
//        (indicesBuffer as Buffer).limit(faceCount * 3)
//        softBody.getVertices(verticesBuffer, vertCount, mesh!!.vertexSize, 0)
//        softBody.getIndices(indicesBuffer, faceCount)
//        val verts = FloatArray(vertCount * vertSize)
//        val uvOffset = mesh!!.getVertexAttribute(VertexAttributes.Usage.TextureCoordinates).offset / 4
//        val normalOffset = mesh!!.getVertexAttribute(VertexAttributes.Usage.Normal).offset / 4
//        mesh!!.getVertices(verts)
//        for (i in 0 until vertCount) {
//            verts[i * vertSize + normalOffset] = 0f
//            verts[i * vertSize + normalOffset + 1] = 1f
//            verts[i * vertSize + normalOffset + 2] = 0f
//            verts[i * vertSize + uvOffset] = (verts[i * vertSize] - x0) / (x1 - x0)
//            verts[i * vertSize + uvOffset + 1] = (verts[i * vertSize + 2] - z0) / (z1 - z0)
//        }
//        mesh!!.setVertices(verts)
//        texture = Texture(Gdx.files.internal("data/badlogic.jpg"))
//        val builder = ModelBuilder()
//        builder.begin()
//        builder.part(
//            MeshPart("", mesh, 0, mesh!!.numIndices, GL20.GL_TRIANGLES),
//            Material(
//                TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(Color.WHITE),
//                FloatAttribute.createShininess(64f), IntAttribute.createCullFace(0)
//            )
//        )
//        model = builder.end()
//        instance = ModelInstance(model)
//        world!!.add(BulletEntity(instance!!, null))
//    }
//
//    override fun dispose() {
//        (world!!.collisionWorld as btSoftRigidDynamicsWorld).removeSoftBody(softBody)
//        softBody!!.dispose()
//        softBody = null
//        super.dispose()
//        worldInfo!!.dispose()
//        worldInfo = null
//        instance = null
//        model!!.dispose()
//        model = null
//        mesh = null
//        texture!!.dispose()
//        texture = null
//    }
//
//    override fun renderWorld() {
//        softBody!!.getVertices(mesh!!.getVerticesBuffer(true), softBody!!.nodeCount, mesh!!.vertexSize, 0)
//        softBody!!.getWorldTransform(instance!!.transform)
//        super.renderWorld()
//
//// modelBatch.begin(camera);
//// world.render(modelBatch, lights);
//// modelBatch.render(instance, lights);
//// modelBatch.end();
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y, 20f)
//        return true
//    }
//}