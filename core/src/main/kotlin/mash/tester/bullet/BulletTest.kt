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

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.FloatCounter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.PerformanceCounter

/** @author xoppa
 */
open class BulletTest : InputAdapter(), ApplicationListener, GestureDetector.GestureListener {
    var performance = StringBuilder()
    var instructions =
        "Tap to shoot\nLong press to toggle debug mode\nSwipe for next test\nCtrl+drag to rotate\nScroll to zoom"
    var performanceCounter = PerformanceCounter(this.javaClass.simpleName)
    var fpsCounter = FloatCounter(5)
    lateinit var camera: PerspectiveCamera
    override fun create() {}
    override fun resize(width: Int, height: Int) {}
    override fun render() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}
    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        return false
    }

    override fun pinch(
        initialPointer1: Vector2,
        initialPointer2: Vector2,
        pointer1: Vector2,
        pointer2: Vector2
    ): Boolean {
        return false
    }

    override fun pinchStop() {}
}