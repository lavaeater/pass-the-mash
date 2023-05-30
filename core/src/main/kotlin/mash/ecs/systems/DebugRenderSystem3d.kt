package mash.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.ashley.allOf
import ktx.math.plus
import ktx.math.vec3
import net.mgsx.gltf.scene3d.scene.Scene
import threedee.ecs.components.CharacterControlComponent
import threedee.ecs.components.MotionStateComponent
import threedee.ecs.components.SceneComponent

class DebugRenderSystem3d(private val viewport: Viewport, private val bulletWorld: btDynamicsWorld) : IteratingSystem(
    allOf(
        MotionStateComponent::class
    ).get()
) {
    val debugDrawer = DebugDrawer().apply {
        debugMode = 1 or 2 or 4 or 8 or 16 or 32 or 64 or 128 or 256 or 512 or 1024
        bulletWorld.debugDrawer = this
    }

    val characterControlComponentFamily = allOf(CharacterControlComponent::class).get()
    val controlledEntity by lazy { engine.getEntitiesFor(characterControlComponentFamily).first() }
    val controlComponent by lazy { CharacterControlComponent.get(controlledEntity) }

    private val forwardColor = vec3(0f, 0f, 1f)
    private val upColor = vec3(0f, 1f, 0f)
    private val rightColor = vec3(1f, 0f, 0f)

    private val camera by lazy { viewport.camera }

    var mouseScreenPosition = vec3()
        get() {
            field.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            return field
        }
        private set

    var mousePosition = vec3()
        get() {
            field.set(mouseScreenPosition)
            return camera.unproject(field)
        }
        private set

    override fun update(deltaTime: Float) {
        debugDrawer.begin(viewport)
//        bulletWorld.debugDrawWorld()
        super.update(deltaTime)
        drawDebugNodes()
        debugDrawer.end()
    }

    private fun drawDebugNodes() {

    }

    private val rotationDirection = vec3()

    private val someTempVector = vec3()
    private val currentRotation = Quaternion()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val motionState = MotionStateComponent.get(entity)
        //Draw the normals!
        if (SceneComponent.has(entity)) {
            val scene = SceneComponent.get(entity).scene
            drawSkeleton(scene)
            scene.modelInstance.transform.getTranslation(sceneWorldPosition)
            scene.modelInstance.transform.getRotation(currentRotation)
        }

        debugDrawer.drawLine(sceneWorldPosition, sceneWorldPosition + motionState.forward.cpy().scl(2f), forwardColor)
        debugDrawer.drawLine(sceneWorldPosition, sceneWorldPosition + motionState.up.cpy().scl(2f), upColor)
        debugDrawer.drawLine(sceneWorldPosition, sceneWorldPosition + motionState.right.cpy().scl(2f), rightColor)

        debugDrawer.drawSphere(controlComponent.intersection, 0.1f, vec3(1f, 1f, 1f))
        debugDrawer.drawLine(
            sceneWorldPosition,
            sceneWorldPosition + controlComponent.lookDirection.cpy().scl(5f),
            rightColor
        )
    }

    private val sceneWorldPosition = vec3()
    private val parentTranslation = vec3()
    private val childTranslation = vec3()
    private fun drawNode(parent: Node, actualNode: Node, worldTransform: Matrix4) {
        val from = parent.globalTransform.getTranslation(parentTranslation)
        val to = actualNode.globalTransform.getTranslation(childTranslation)

        from.mul(worldTransform)
        to.mul(worldTransform)

        debugDrawer.drawLine(from, to, vec3(1f, 1f, 0f))
        for (child in actualNode.children) {
            drawNode(actualNode, child, worldTransform)
        }
    }

    private fun drawSkeleton(scene: Scene) {
        if (scene.modelInstance.nodes.any()) {
            val firstNode = scene.modelInstance.nodes.first()
            firstNode.children.forEach { drawNode(firstNode, it, scene.modelInstance.transform) }
        }
    }
}

object GlobalVectorBullshit {
    val tempVector3 = vec3()
}

fun Vector3.inXZPlane(): Vector3 {
    return GlobalVectorBullshit.tempVector3.setZero().set(this.x, 0f, this.z)
}
