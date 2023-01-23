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
//import com.badlogic.gdx.assets.AssetManager
//import com.badlogic.gdx.graphics.*
//import com.badlogic.gdx.graphics.g2d.SpriteBatch
//import com.badlogic.gdx.graphics.g3d.*
//import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
//import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
//import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer
//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
//import com.badlogic.gdx.graphics.profiling.GLProfiler
//import com.badlogic.gdx.math.RandomXS128
//import com.badlogic.gdx.math.Vector3
//import com.badlogic.gdx.math.collision.BoundingBox
//import com.badlogic.gdx.physics.bullet.collision.*
//import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
//import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
//import com.badlogic.gdx.utils.Array
//import com.badlogic.gdx.utils.StringBuilder
//
///** @author jsjolund
// */
//class OcclusionCullingTest : BaseBulletTest() {
//    /** Types of culling to use in the test application  */
//    private enum class CullingPolicy {
//        /** Occlusion culling, renders only entities which are visible from the viewpoint of a camera. Objects which are hidden
//         * behind other objects (occluded) do not need to be rendered.  */
//        OCCLUSION,
//
//        /** No culling, renders all objects.  */
//        NONE,
//
//        /** Simple culling which loops through all entities in the world, and checks if the radius of the object bounding box is
//         * inside the camera frustum. Hidden surfaces are not taken into account. Bullet is not used.  */
//        SIMPLE,
//
//        /** Same as [CullingPolicy.SIMPLE], except instead of checking each object in the world, the process is accelerated by
//         * the Bullet broadphase bounding volume tree.  */
//        KDOP;
//
//        operator fun next(): CullingPolicy {
//            return `val`[(ordinal + 1) % `val`.size]
//        }
//
//        companion object {
//            private val `val` = values()
//        }
//    }
//
//    private val frustumCamPos = Vector3(0f, 4f, FRUSTUM_MOVE_RADIUS)
//    private var frustumCamAngleY = 0f
//    private var frustumCam: PerspectiveCamera? = null
//    private var frustumInstance: ModelInstance? = null
//    private var overviewCam: PerspectiveCamera? = null
//    val visibleEntities = Array<BulletEntity?>()
//
//    // Program state variables
//    private var cullingPolicy = CullingPolicy.OCCLUSION
//    private var bufferExtentIndex = 0
//    private var state = 0
//
//    // For occlusion culling
//    private var oclBuffer: OcclusionBuffer? = null
//    private var occlusionCuller: OcclusionCuller? = null
//    private var broadphase: btDbvtBroadphase? = null
//    private val rng = RandomXS128(0)
//
//    // For drawing occlusion buffer debug image
//    private var shapeRenderer: ShapeRenderer? = null
//    private var spriteBatch: SpriteBatch? = null
//    private var glProfiler: GLProfiler? = null
//
//    /** Adds an occluder entity of specified type
//     *
//     * @param type Type name
//     * @param rotationY Rotation on Y axis in degrees
//     * @param position The world position
//     * @return The added entity
//     */
//    private fun addOccluder(type: String, rotationY: Float, position: Vector3): BulletEntity? {
//        val e = world!!.add(type, 0f, 0f, 0f)
//        e!!.body!!.worldTransform = e.transform!!.setToRotation(Vector3.Y, rotationY).setTranslation(position)
//        e.body!!.collisionFlags = e.body!!.collisionFlags or CF_OCCLUDER_OBJECT.toInt()
//        e.color = Color.RED
//        return e
//    }
//
//    /** Adds an occludee entity of random type at a random place on the ground.
//     *
//     * @param dynamic If true, entity body will be dynamic (mass > 0)
//     * @return The added entity
//     */
//    private fun addRandomOccludee(dynamic: Boolean): BulletEntity? {
//        // Add occludee to world
//        val entity = world!!.add(getRandomOccludeeType(dynamic), 0f, 0f, 0f)
//        entity!!.color = Color.WHITE
//        // Random rotation
//        val rotationY = rng.nextFloat() * 360f
//        // Random ground position
//        val position: Vector3 = BaseBulletTest.Companion.tmpV1
//        val maxDstX = (GROUND_DIM.x * 0.49f).toInt()
//        position.x = (rng.nextInt(maxDstX) * if (rng.nextBoolean()) 1 else -1).toFloat()
//        position.z = (rng.nextInt(maxDstX) * if (rng.nextBoolean()) 1 else -1).toFloat()
//        position.y = entity.boundingBox.getDimensions(BaseBulletTest.Companion.tmpV2).y * 0.5f
//        entity.modelInstance!!.transform.setToRotation(Vector3.Y, rotationY).setTranslation(position)
//        entity.body!!.worldTransform = entity.modelInstance!!.transform
//        return entity
//    }
//
//    override fun create() {
//        Gdx.input.setOnscreenKeyboardVisible(true)
//        super.create()
//        glProfiler = GLProfiler(Gdx.graphics)
//        glProfiler!!.enable()
//        val sb = StringBuilder()
//        sb.append("Swipe for next test\n")
//        sb.append("Long press to toggle debug mode\n")
//        sb.append("Ctrl+drag to rotate\n")
//        sb.append("Scroll to zoom\n")
//        sb.append("Tap to spawn dynamic entity, press\n")
//        sb.append("'0' to spawn ").append(KEY_SPAWN_OCCLUDEE_AMOUNT).append(" static entities\n")
//        sb.append("'1' to set normal/disabled/occlusion-culling\n")
//        sb.append("'2' to change camera\n")
//        sb.append("'3' to toggle camera movement\n")
//        sb.append("'4' to cycle occlusion buffer sizes\n")
//        sb.append("'5' to toggle occlusion buffer image\n")
//        sb.append("'6' to toggle shadows\n")
//        instructions = sb.toString()
//        val assets = AssetManager()
//        disposables.add(assets)
//        for (modelName in OCCLUDEE_PATHS_DYNAMIC) assets.load(modelName, Model::class.java)
//        assets.load(DEFAULT_TEX_PATH, Texture::class.java)
//        val shadowCamera = (light as DirectionalShadowLight).camera
//        shadowCamera.viewportHeight = 120f
//        shadowCamera.viewportWidth = shadowCamera.viewportHeight
//
//        // User controlled camera
//        overviewCam = camera
//        overviewCam!!.position.set(overviewCam!!.direction).nor().scl(-100f)
//        overviewCam!!.lookAt(Vector3.Zero)
//        camera!!.far *= 2f
//        overviewCam!!.far = camera!!.far
//        overviewCam!!.update(true)
//
//        // Animated frustum camera model
//        frustumCam = PerspectiveCamera(FRUSTUM_CAMERA_FOV, camera!!.viewportWidth, camera!!.viewportHeight)
//        frustumCam!!.far = FRUSTUM_CAMERA_FAR
//        frustumCam!!.update(true)
//        val frustumModel: Model = FrustumCullingTest.Companion.createFrustumModel(*frustumCam!!.frustum.planePoints)
//        frustumModel.materials.first().set(ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE))
//        disposables.add(frustumModel)
//        frustumInstance = ModelInstance(frustumModel)
//        spriteBatch = SpriteBatch()
//        disposables.add(spriteBatch)
//        shapeRenderer = ShapeRenderer()
//        disposables.add(shapeRenderer)
//        oclBuffer = OcclusionBuffer(OCL_BUFFER_EXTENTS[0], OCL_BUFFER_EXTENTS[0])
//        disposables.add(oclBuffer)
//        occlusionCuller = object : OcclusionCuller() {
//            override fun isOccluder(`object`: btCollisionObject): Boolean {
//                return `object`.collisionFlags and CF_OCCLUDER_OBJECT.toInt() != 0
//            }
//
//            override fun onObjectVisible(`object`: btCollisionObject) {
//                visibleEntities.add(world!!.entities[`object`.userValue])
//            }
//        }
//        disposables.add(occlusionCuller)
//
//        // Add occluder walls
//        val occluderModel = modelBuilder.createBox(
//            OCCLUDER_DIM.x, OCCLUDER_DIM.y, OCCLUDER_DIM.z,
//            Material(ColorAttribute.createDiffuse(Color.WHITE)),
//            (
//                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )
//        disposables.add(occluderModel)
//        world!!.addConstructor(
//            "wall", BulletConstructor(
//                occluderModel, 0f, btBoxShape(
//                    BaseBulletTest.Companion.tmpV1.set(
//                        OCCLUDER_DIM
//                    ).scl(0.5f)
//                )
//            )
//        )
//        val y = OCCLUDER_DIM.y * 0.5f
//        addOccluder("wall", 0f, BaseBulletTest.Companion.tmpV1.set(20f, y, 0f))
//        addOccluder("wall", -60f, BaseBulletTest.Companion.tmpV1.set(10f, y, 20f))
//        addOccluder("wall", 60f, BaseBulletTest.Companion.tmpV1.set(10f, y, -20f))
//        addOccluder("wall", 0f, BaseBulletTest.Companion.tmpV1.set(-20f, y, 0f))
//        addOccluder("wall", 60f, BaseBulletTest.Companion.tmpV1.set(-10f, y, 20f))
//        addOccluder("wall", -60f, BaseBulletTest.Companion.tmpV1.set(-10f, y, -20f))
//
//        // Add ground
//        val groundModel = modelBuilder.createBox(
//            GROUND_DIM.x, GROUND_DIM.y, GROUND_DIM.z,
//            Material(ColorAttribute.createDiffuse(Color.WHITE)),
//            (
//                    VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
//        )
//        val groundShape: btCollisionShape = btBoxShape(BaseBulletTest.Companion.tmpV1.set(GROUND_DIM).scl(0.5f))
//        world!!.addConstructor("big_ground", BulletConstructor(groundModel, 0f, groundShape))
//        val e = world!!.add("big_ground", 0f, -GROUND_DIM.y * 0.5f, 0f)
//        e!!.body!!.friction = 1f
//        e.color = Color.FOREST
//
//        // Occludee entity constructors. Scale models uniformly and set a default diffuse texture.
//        val bb = BoundingBox()
//        assets.finishLoadingAsset<Any>(DEFAULT_TEX_PATH)
//        val defaultTexture = TextureAttribute(
//            TextureAttribute.Diffuse,
//            assets.get(DEFAULT_TEX_PATH, Texture::class.java)
//        )
//        for (i in OCCLUDEE_PATHS_DYNAMIC.indices) {
//            val modelPath = OCCLUDEE_PATHS_DYNAMIC[i]
//            OCCLUDEE_PATHS_STATIC[i] = "static$modelPath"
//            assets.finishLoadingAsset<Any>(modelPath)
//            val model = assets.get(modelPath, Model::class.java)
//            if (!model.materials.first().has(TextureAttribute.Diffuse)) model.materials.first().set(defaultTexture)
//            val dim = model.calculateBoundingBox(bb).getDimensions(BaseBulletTest.Companion.tmpV1)
//            val scaleFactor = OCCLUDEE_MAX_EXTENT / Math.max(dim.x, Math.max(dim.y, dim.z))
//            for (node in model.nodes) node.scale.scl(scaleFactor)
//            val shape: btCollisionShape = btBoxShape(dim.scl(scaleFactor * 0.5f))
//            world!!.addConstructor(modelPath, BulletConstructor(model, 1f, shape))
//            world!!.addConstructor(OCCLUDEE_PATHS_STATIC[i]!!, BulletConstructor(model, 0f, shape))
//        }
//        // Add occludees
//        for (i in 0 until STARTING_OCCLUDEE_AMOUNT) addRandomOccludee(false)
//    }
//
//    override fun createWorld(): BulletWorld? {
//        val collisionConfig = btDefaultCollisionConfiguration()
//        val dispatcher = btCollisionDispatcher(collisionConfig)
//        val solver = btSequentialImpulseConstraintSolver()
//        broadphase = btDbvtBroadphase()
//        val collisionWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig)
//        return BulletWorld(collisionConfig, dispatcher, broadphase, solver, collisionWorld)
//    }
//
//    override fun dispose() {
//        Gdx.input.setOnscreenKeyboardVisible(false)
//        glProfiler!!.disable()
//        visibleEntities.clear()
//        rng.setSeed(0)
//        state = 0
//        bufferExtentIndex = 0
//        cullingPolicy = CullingPolicy.OCCLUSION
//        super.dispose()
//    }
//
//    /** Checks if entity is inside camera frustum.
//     *
//     * @param entity An entity
//     * @return True if entity is inside camera frustum
//     */
//    private fun entityInFrustum(entity: BulletEntity?): Boolean {
//        entity!!.modelInstance!!.transform.getTranslation(BaseBulletTest.Companion.tmpV1)
//        return frustumCam!!.frustum.sphereInFrustum(
//            BaseBulletTest.Companion.tmpV1.add(
//                entity.boundingBox.getCenter(
//                    BaseBulletTest.Companion.tmpV2
//                )
//            ), entity.boundingBoxRadius
//        )
//    }
//
//    /** Get the type name of a random occludee entity.
//     *
//     * @param dynamic If true, the name of a dynamic entity will be returned (mass > 0)
//     * @return Name of a random entity type
//     */
//    private fun getRandomOccludeeType(dynamic: Boolean): String {
//        val i = rng.nextInt(OCCLUDEE_PATHS_STATIC.size)
//        return if (dynamic) OCCLUDEE_PATHS_DYNAMIC[i] else OCCLUDEE_PATHS_STATIC[i]!!
//    }
//
//    override fun keyTyped(character: Char): Boolean {
//        oclBuffer!!.clear()
//        when (character) {
//            '0' -> {
//                var i = 0
//                while (i < KEY_SPAWN_OCCLUDEE_AMOUNT) {
//                    addRandomOccludee(false)
//                    i++
//                }
//            }
//
//            '1' -> cullingPolicy = cullingPolicy.next()
//            '2' -> {
//                state = state xor USE_FRUSTUM_CAM
//                camera = if (state and USE_FRUSTUM_CAM == USE_FRUSTUM_CAM) frustumCam else overviewCam
//            }
//
//            '3' -> state = state xor PAUSE_FRUSTUM_CAM
//            '4' -> {
//                oclBuffer!!.dispose()
//                bufferExtentIndex = (bufferExtentIndex + 1) % OCL_BUFFER_EXTENTS.size
//                val extent = OCL_BUFFER_EXTENTS[bufferExtentIndex]
//                oclBuffer = OcclusionBuffer(extent, extent)
//            }
//
//            '5' -> state = state xor SHOW_DEBUG_IMAGE
//            '6' -> {
//                BaseBulletTest.Companion.shadows = !BaseBulletTest.Companion.shadows
//                // Clear the old shadows
//                visibleEntities.clear()
//                renderShadows()
//            }
//        }
//        return true
//    }
//
//    override fun render() {
//        super.render()
//        if (state and SHOW_DEBUG_IMAGE == SHOW_DEBUG_IMAGE) renderOclDebugImage()
//        performance.append(", Culling: ").append(cullingPolicy.name)
//        performance.append(", Visible: ").append(visibleEntities.size).append("/").append(world!!.entities.size)
//        performance.append(", Buffer: ").append(OCL_BUFFER_EXTENTS[bufferExtentIndex]).append("px ")
//        performance.append(", GL Draw calls: ").append(glProfiler!!.drawCalls)
//        glProfiler!!.reset()
//    }
//
//    private fun renderOclDebugImage() {
//        val oclDebugTexture = oclBuffer!!.drawDebugTexture()
//        spriteBatch!!.begin()
//        spriteBatch!!.draw(oclDebugTexture, 0f, 0f)
//        spriteBatch!!.end()
//        shapeRenderer!!.begin(ShapeType.Line)
//        shapeRenderer!!.color = Color.DARK_GRAY
//        shapeRenderer!!.rect(0f, 0f, oclDebugTexture!!.regionWidth.toFloat(), oclDebugTexture.regionHeight.toFloat())
//        shapeRenderer!!.end()
//    }
//
//    private fun renderShadows() {
//        (light as DirectionalShadowLight).begin(Vector3.Zero, camera!!.direction)
//        shadowBatch!!.begin((light as DirectionalShadowLight).camera)
//        world!!.render(shadowBatch!!, null, visibleEntities)
//        shadowBatch!!.end()
//        (light as DirectionalShadowLight).end()
//    }
//
//    override fun renderWorld() {
//        visibleEntities.clear()
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.start()
//        if (cullingPolicy == CullingPolicy.NONE) {
//            visibleEntities.addAll(world!!.entities)
//        } else if (cullingPolicy == CullingPolicy.SIMPLE) {
//            for (entity in world!!.entities) if (entityInFrustum(entity)) visibleEntities.add(entity)
//        } else if (cullingPolicy == CullingPolicy.OCCLUSION) {
//            oclBuffer!!.clear()
//            occlusionCuller!!.performOcclusionCulling(broadphase, oclBuffer, frustumCam)
//        } else if (cullingPolicy == CullingPolicy.KDOP) {
//            occlusionCuller!!.performKDOPCulling(broadphase, frustumCam)
//        }
//        if (world!!.performanceCounter != null) world!!.performanceCounter!!.stop()
//        if (BaseBulletTest.Companion.shadows) renderShadows()
//        modelBatch!!.begin(camera)
//        world!!.render(modelBatch!!, environment, visibleEntities)
//        if (state and USE_FRUSTUM_CAM != USE_FRUSTUM_CAM) modelBatch!!.render(frustumInstance)
//        modelBatch!!.end()
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        val entity = shoot(getRandomOccludeeType(true), x, y, 30f)
//        entity!!.color = Color.WHITE
//        return true
//    }
//
//    override fun update() {
//        super.update()
//        // Transform the frustum camera
//        if (state and PAUSE_FRUSTUM_CAM == PAUSE_FRUSTUM_CAM) return
//        val dt = Gdx.graphics.deltaTime
//        frustumInstance!!.transform.idt().rotate(
//            Vector3.Y,
//            (frustumCamAngleY + dt * FRUSTUM_ANG_SPEED) % 360.also { frustumCamAngleY = it.toFloat() })
//        frustumCam!!.direction[0f, 0f] = -1f
//        frustumCam!!.up.set(Vector3.Y)
//        frustumCam!!.position.set(Vector3.Zero)
//        frustumCam!!.rotate(frustumInstance!!.transform)
//        val frustumCamPosY = frustumCamPos.y
//        frustumCamPos.add(
//            BaseBulletTest.Companion.tmpV1.set(Vector3.Y).crs(BaseBulletTest.Companion.tmpV2.set(frustumCamPos).nor())
//                .scl(dt * FRUSTUM_LIN_SPEED)
//        ).nor()
//            .scl(FRUSTUM_MOVE_RADIUS)
//        frustumCamPos.y = frustumCamPosY
//        frustumCam!!.position.set(frustumCamPos)
//        frustumInstance!!.transform.setTranslation(frustumCamPos)
//        frustumCam!!.update()
//    }
//
//    companion object {
//        /** Collision objects with this collision flag can occlude other objects  */
//        const val CF_OCCLUDER_OBJECT: Short = 512
//
//        /** Amount of occludee entities to spawn at program start  */
//        private const val STARTING_OCCLUDEE_AMOUNT = 300
//
//        /** Number of occludee entities to spawn at key press  */
//        private const val KEY_SPAWN_OCCLUDEE_AMOUNT = 100
//
//        /** Occlusion depth buffer image size  */
//        private val OCL_BUFFER_EXTENTS = intArrayOf(128, 256, 512, 32, 64)
//
//        // Animated frustum camera settings
//        private const val FRUSTUM_CAMERA_FAR = 50f
//        private const val FRUSTUM_CAMERA_FOV = 60f
//        private const val FRUSTUM_ANG_SPEED = 360f / 15f
//        private const val FRUSTUM_LIN_SPEED = -6f
//        private const val FRUSTUM_MOVE_RADIUS = 12f
//
//        // Occludee models and textures used in test
//        private const val DEFAULT_TEX_PATH = "data/g3d/checkboard.png"
//        private val OCCLUDEE_PATHS_DYNAMIC = arrayOf(
//            "data/car.obj", "data/wheel.obj", "data/cube.obj",
//            "data/g3d/ship.obj", "data/g3d/shapes/sphere.g3dj", "data/g3d/shapes/torus.g3dj"
//        )
//        private val OCCLUDEE_PATHS_STATIC = arrayOfNulls<String>(OCCLUDEE_PATHS_DYNAMIC.size)
//        private const val OCCLUDEE_MAX_EXTENT = 1.5f
//        private val OCCLUDER_DIM = Vector3(1f, 6f, 20f)
//        private val GROUND_DIM = Vector3(120f, 1f, 120f)
//        private const val USE_FRUSTUM_CAM = 1
//        private const val PAUSE_FRUSTUM_CAM = 2
//        private const val SHOW_DEBUG_IMAGE = 4
//    }
//}