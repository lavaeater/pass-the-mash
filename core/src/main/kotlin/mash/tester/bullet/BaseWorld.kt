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

import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectMap

/** @author xoppa No physics, simple base class for rendering a bunch of entities.
 */
open class BaseWorld<T : BaseEntity> : Disposable {
    abstract class Constructor<T : BaseEntity> : Disposable {
        lateinit var model: Model
        abstract fun construct(x: Float, y: Float, z: Float): T
        abstract fun construct(transform: Matrix4): T
    }

    private val constructors = ObjectMap<String, Constructor<T>>()
    val entities = Array<T>()
    private val models = Array<Model>()
    fun addConstructor(name: String, constructor: Constructor<T>) {
        constructors.put(name, constructor)
        if (!models.contains(constructor.model, true)) models.add(constructor.model)
    }

    fun getConstructor(name: String): Constructor<T> {
        return constructors.get(name)
    }

    open fun add(entity: T) {
        entities.add(entity)
    }

    fun add(type: String, x: Float, y: Float, z: Float): T {
        val entity = constructors.get(type).construct(x, y, z)
        add(entity)
        return entity
    }

    fun add(type: String, transform: Matrix4?): T {
        val entity = constructors.get(type).construct(transform!!)
        add(entity)
        return entity
    }

    fun render(batch: ModelBatch, lights: Environment?) {
        render(batch, lights, entities)
    }

    open fun render(batch: ModelBatch, lights: Environment?, entities: Iterable<T>) {
        for (e in entities) {
            batch.render(e!!.modelInstance, lights)
        }
    }

    fun render(batch: ModelBatch, lights: Environment?, entity: T) {
        batch.render(entity!!.modelInstance, lights)
    }

    open fun update() {}
    override fun dispose() {
        for (i in 0 until entities.size) entities[i]!!.dispose()
        entities.clear()
        for (constructor in constructors.values()) constructor.dispose()
        constructors.clear()
        models.clear()
    }
}