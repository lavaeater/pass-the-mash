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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape
import com.badlogic.gdx.physics.bullet.collision.btSphereShape

/** @author xoppa
 */
class MeshShapeTest : BaseBulletTest() {
    override fun create() {
        super.create()
        val sphereModel = modelBuilder.createSphere(
            0.5f, 0.5f, 0.5f, 8, 8,
            Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)),
            (
                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        )
        disposables.add(sphereModel)
        val sphereConstructor = BulletConstructor(sphereModel, 0.25f, btSphereShape(0.25f))
        sphereConstructor.bodyInfo!!.restitution = 1f
        world!!.addConstructor("sphere", sphereConstructor)
        val sceneModel = objLoader.loadModel(Gdx.files.internal("data/scene.obj"))
        disposables.add(sceneModel)
        val sceneConstructor = BulletConstructor(
            sceneModel, 0f,
            btBvhTriangleMeshShape(sceneModel.meshParts)
        )
        sceneConstructor.bodyInfo!!.restitution = 0.25f
        world!!.addConstructor("scene", sceneConstructor)
        world!!.add("scene", Matrix4().setToTranslation(0f, 2f, 0f).rotate(Vector3.Y, -90f))!!.setColor(
            0.25f + 0.5f * Math.random().toFloat(),
            0.25f + 0.5f * Math.random().toFloat(),
            0.25f + 0.5f * Math.random().toFloat(),
            1f
        )
        world!!.add("ground", 0f, 0f, 0f)!!
            .setColor(
                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
                0.25f + 0.5f * Math.random().toFloat(), 1f
            )
        for (x in -3..6) {
            for (z in -5..4) {
                world!!.add("sphere", x.toFloat(), 10f + Math.random().toFloat() * 0.1f, z.toFloat())!!
                    .setColor(
                        0.5f + 0.5f * Math.random().toFloat(),
                        0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(), 1f
                    )
            }
        }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }
}