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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable

/** @author xoppa Base class specifying only a renderable entity
 */
abstract class BaseEntity : Disposable {
    var transform: Matrix4? = null
    var modelInstance: ModelInstance? = null
    var color = Color(1f, 1f, 1f, 1f)

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color[r, g, b] = a
        if (modelInstance != null) {
            for (m in modelInstance!!.materials) {
                val ca = m[ColorAttribute.Diffuse] as ColorAttribute
                ca?.color?.set(r, g, b, a)
            }
        }
    }
}