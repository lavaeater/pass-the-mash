package mash.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import depth.ecs.components.Direction
import depth.ecs.components.Rotation
import ktx.ashley.mapperFor

class KeyboardControlComponent: Component, Pool.Poolable {

    val directionThing = DirectionThing()
    fun has(direction: Direction) : Boolean {
        return directionThing.has(direction)
    }

    fun has(rotation: Rotation): Boolean {
        return directionThing.has(rotation)
    }

    fun add(direction: Direction) {
        directionThing.add(direction)
    }
    fun remove(direction: Direction) {
        directionThing.remove(direction)
    }

    fun add(rotation: Rotation) {
        directionThing.add(rotation)
    }
    fun remove(rotation: Rotation) {
        directionThing.remove(rotation)
    }

    override fun reset() {
        directionThing.clear()
    }

    companion object {
        val mapper = mapperFor<KeyboardControlComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): KeyboardControlComponent {
            return mapper.get(entity)
        }
    }
}
