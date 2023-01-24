package depth.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor
import ktx.math.vec3

class MotionState(val transform: Matrix4) : btMotionState() {
    val position = vec3()
    val forward = vec3()
    val up = vec3()
    val right = vec3()
    private val tmpVector = vec3()

    val backwards: Vector3
        get() = tmpVector.set(forward).rotate(Vector3.Y, 180f)
    val down: Vector3
        get() = tmpVector.set(up).rotate(Vector3.X, 180f)
    val left: Vector3
        get() = tmpVector.set(right).rotate(Vector3.Y, 180f)

    override fun getWorldTransform(worldTrans: Matrix4) {
        worldTrans.set(transform)
    }

    override fun setWorldTransform(worldTrans: Matrix4) {
        transform.set(worldTrans)
        transform.getTranslation(position)
        getDirection(transform)
    }

    private fun getDirection(transform: Matrix4?) {
        forward.set(Vector3.Z)
        up.set(Vector3.Y)
        right.set(Vector3.X)
        forward.rot(transform).nor()
        up.rot(transform).nor()
        right.rot(transform).nor()
    }
}

class MotionStateComponent : Component, Poolable {

    private var _motionState: MotionState? = null
    var motionState: MotionState
        get() = _motionState!!
        set(value) {
            _motionState = value
        }

    val position get() = motionState.position
    val forward get() = motionState.forward
    val up get() = motionState.up
    val right get() = motionState.right
    val backwards get() = motionState.backwards
    val down get() = motionState.down
    val left get() = motionState.left


    override fun reset() {
        _motionState?.dispose()
    }

    companion object {
        val mapper = mapperFor<MotionStateComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): MotionStateComponent {
            return mapper.get(entity)
        }
    }
}
