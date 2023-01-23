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
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape
import com.badlogic.gdx.physics.bullet.collision.btShapeHull

/** @author xoppa
 */
class ConvexHullTest : BaseBulletTest() {
    override fun create() {
        super.create()
        val carModel = objLoader.loadModel(Gdx.files.internal("data/car.obj"))
        disposables.add(carModel)
        carModel.materials[0].clear()
        carModel.materials[0][ColorAttribute.createDiffuse(Color.WHITE)] =
            ColorAttribute.createSpecular(Color.WHITE)
        world!!.addConstructor("car", BulletConstructor(carModel, 5f, createConvexHullShape(carModel, true)))

        // Create the entities
        world!!.add("ground", 0f, 0f, 0f)!!
            .setColor(
                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
                0.25f + 0.5f * Math.random().toFloat(), 1f
            )
        var y = 10f
        while (y < 50f) {
            world!!.add("car", -2f + Math.random().toFloat() * 4f, y, -2f + Math.random().toFloat() * 4f)!!
                .setColor(
                    0.25f + 0.5f * Math.random().toFloat(),
                    0.25f + 0.5f * Math.random().toFloat(),
                    0.25f + 0.5f * Math.random().toFloat(),
                    1f
                )
            y += 5f
        }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }

    companion object {
        fun createConvexHullShape(model: Model, optimize: Boolean): btConvexHullShape {
            val mesh = model.meshes[0]
            val shape = btConvexHullShape(
                mesh.verticesBuffer, mesh.numVertices,
                mesh.vertexSize
            )
            if (!optimize) return shape
            // now optimize the shape
            val hull = btShapeHull(shape)
            hull.buildHull(shape.margin)
            val result = btConvexHullShape(hull)
            // delete the temporary shape
            shape.dispose()
            hull.dispose()
            return result
        }
    }
}