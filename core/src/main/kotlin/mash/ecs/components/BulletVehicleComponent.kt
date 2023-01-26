package mash.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import mash.bullet.BulletVehicle

class BulletVehicleComponent: Component, Pool.Poolable {

    private var _bulletVehicle: BulletVehicle? = null
    var bulletVehicle: BulletVehicle
        get() = _bulletVehicle!!
        set(value) {
            _bulletVehicle = value
        }

    val vehicle: btRaycastVehicle
        get() = _bulletVehicle?.vehicle!!

    var currentForce = 0f
    var currentAngle = 0f



    override fun reset() {
        _bulletVehicle = null
        currentAngle = 0f
        currentForce = 0f
    }

    companion object {
        val mapper = mapperFor<BulletVehicleComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): BulletVehicleComponent {
            return mapper.get(entity)
        }
    }
}