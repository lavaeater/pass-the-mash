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

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable

/** @author xoppa
 */
open class BaseBulletTest : BulletTest() {
    lateinit var environment: Environment
    lateinit var light: DirectionalLight
    lateinit  var shadowBatch: ModelBatch
    lateinit var world: BulletWorld
    var objLoader = ObjLoader()
    var modelBuilder = ModelBuilder()
    lateinit  var modelBatch: ModelBatch
    var disposables = Array<Disposable>()
    private var debugMode = DebugDrawModes.DBG_NoDebug
    open fun createWorld(): BulletWorld {
        return BulletWorld()
    }

    override fun create() {
        init()
        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f))
        light = if (shadows) DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) else DirectionalLight()
        light[0.8f, 0.8f, 0.8f, -0.5f, -1f] = 0.7f
        environment.add(light)
        if (shadows) environment.shadowMap = light as DirectionalShadowLight?
        shadowBatch = ModelBatch(DepthShaderProvider())
        modelBatch = ModelBatch()
        world = createWorld()
        world.performanceCounter = performanceCounter
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        camera = if (width > height) PerspectiveCamera(67f, 3f * width / height, 3f) else PerspectiveCamera(
            67f,
            3f,
            3f * height / width
        )
        camera.position[10f, 10f] = 10f
        camera.lookAt(0f, 0f, 0f)
        camera.update()

        // Create some simple models
        val groundModel = modelBuilder.createRect(
            20f, 0f, -20f, -20f, 0f, -20f, -20f, 0f, 20f, 20f, 0f, 20f, 0f, 1f, 0f,
            Material(
                ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(16f)
            ),
            (
                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        )
        disposables.add(groundModel)
        val boxModel = modelBuilder.createBox(
            1f, 1f, 1f, Material(
                ColorAttribute.createDiffuse(Color.WHITE),
                ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)
            ), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        )
        disposables.add(boxModel)

        // Add the constructors
        world.addConstructor("ground", BulletConstructor(groundModel, 0f)) // mass = 0: static body
        world.addConstructor("box", BulletConstructor(boxModel, 1f)) // mass = 1kg: dynamic body
        world.addConstructor("staticbox", BulletConstructor(boxModel, 0f)) // mass = 0: static body
    }

    override fun dispose() {
        world.dispose()
        for (disposable in disposables) disposable.dispose()
        disposables.clear()
        modelBatch.dispose()
        modelBatch
        shadowBatch.dispose()
        shadowBatch
        if (shadows) (light as DirectionalShadowLight).dispose()
        light
        super.dispose()
    }

    override fun render() {
        render(true)
    }

    fun render(update: Boolean) {
        fpsCounter.put(Gdx.graphics.framesPerSecond.toFloat())
        if (update) update()
        beginRender(true)
        renderWorld()
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
        if (debugMode != DebugDrawModes.DBG_NoDebug) world.debugMode = debugMode
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        performance.setLength(0)
        performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
            .append((performanceCounter.load.value * 100f).toInt()).append("%")
    }

    protected open fun beginRender(lighting: Boolean) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        camera.update()
    }

    protected open fun renderWorld() {
        if (shadows) {
            (light as DirectionalShadowLight).begin(Vector3.Zero, camera.direction)
            shadowBatch.begin((light as DirectionalShadowLight).camera)
            world.render(shadowBatch, null)
            shadowBatch.end()
            (light as DirectionalShadowLight).end()
        }
        modelBatch.begin(camera)
        world.render(modelBatch, environment)
        modelBatch.end()
    }

    open fun update() {
        world.update()
    }

    @JvmOverloads
    fun shoot(x: Float, y: Float, impulse: Float = 30f): BulletEntity {
        return shoot("box", x, y, impulse)
    }

    fun shoot(what: String, x: Float, y: Float, impulse: Float): BulletEntity {
        // Shoot a box
        val ray = camera.getPickRay(x, y)
        val entity = world.add(what, ray.origin.x, ray.origin.y, ray.origin.z)
        entity.setColor(
            0.5f + 0.5f * Math.random().toFloat(),
            0.5f + 0.5f * Math.random().toFloat(),
            0.5f + 0.5f * Math.random().toFloat(),
            1f
        )
        (entity.body as btRigidBody).applyCentralImpulse(ray.direction.scl(impulse))
        return entity
    }

    fun setDebugMode(mode: Int) {
        world.debugMode = mode.also { debugMode = it }
    }

    fun toggleDebugMode() {
        if (world.debugMode == DebugDrawModes.DBG_NoDebug) setDebugMode(
            DebugDrawModes.DBG_DrawWireframe or DebugDrawModes.DBG_DrawFeaturesText or DebugDrawModes.DBG_DrawText
                    or DebugDrawModes.DBG_DrawContactPoints
        ) else if (world.renderMeshes) world.renderMeshes = false else {
            world.renderMeshes = true
            setDebugMode(DebugDrawModes.DBG_NoDebug)
        }
    }

    override fun longPress(x: Float, y: Float): Boolean {
        toggleDebugMode()
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.ENTER) {
            toggleDebugMode()
            return true
        }
        return super.keyUp(keycode)
    }

    companion object {
        // Set this to the path of the lib to use it on desktop instead of default lib.
        private val customDesktopLib: String? =
            null // "D:\\Xoppa\\code\\libgdx\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
        private var initialized = false
        var shadows = true
        fun init() {
            if (initialized) return
            // Need to initialize bullet before using it.
            if (Gdx.app.type == ApplicationType.Desktop && customDesktopLib != null) {
                System.load(customDesktopLib)
            } else Bullet.init()
            Gdx.app.log("Bullet", "Version = " + LinearMath.btGetVersion())
            initialized = true
        }

        val tmpV1 = Vector3()
        val tmpV2 = Vector3()
    }
}