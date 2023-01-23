package mash.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class BulletVehicle: Component, Pool.Poolable {

    private var _bulletVehicle: btRaycastVehicle? = null
    var bulletVehicle: btRaycastVehicle
        get() = _bulletVehicle!!
        set(value) {
            _bulletVehicle = value
        }

    override fun reset() {
        _bulletVehicle = null
    }

    companion object {
        val mapper = mapperFor<BulletVehicle>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): BulletVehicle {
            return mapper.get(entity)
        }
    }
}