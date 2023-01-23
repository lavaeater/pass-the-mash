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

/** @author Xoppa
 */
open class ShootTest : BaseBulletTest() {
    val BOXCOUNT_X = 5
    val BOXCOUNT_Y = 5
    val BOXCOUNT_Z = 1
    val BOXOFFSET_X = -2.5f
    val BOXOFFSET_Y = 0.5f
    val BOXOFFSET_Z = 0f
    var ground: BulletEntity? = null
    override fun create() {
        super.create()

        // Create the entities
        world!!.add("ground", 0f, 0f, 0f).also { ground = it }!!.setColor(
            0.25f + 0.5f * Math.random().toFloat(),
            0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 1f
        )
        for (x in 0 until BOXCOUNT_X) {
            for (y in 0 until BOXCOUNT_Y) {
                for (z in 0 until BOXCOUNT_Z) {
                    world!!.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)!!
                        .setColor(
                            0.5f + 0.5f * Math.random().toFloat(),
                            0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(), 1f
                        )
                }
            }
        }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }

    override fun dispose() {
        super.dispose()
        ground = null
    }
}