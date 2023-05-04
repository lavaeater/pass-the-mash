package mash.factories

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject

object BulletInstances {
    fun addInstance(instance: btCollisionObject): Int {
        instances.add(instance)
        instance.userIndex = instances.lastIndex
        return instance.userIndex
    }

    fun addEntity(instance: btCollisionObject, entity: Entity): Int {
        instances.add(instance)
        instance.userIndex = instances.lastIndex
        instance.userData = entity
        entities[instance.userIndex] = entity
        return instance.userIndex
    }

    fun getInstance(index: Int) = instances[index]
    fun getEntity(instanceIndex: Int) = entities[instanceIndex]

    private val instances = mutableListOf<btCollisionObject>()
    private val entities = mutableMapOf<Int, Entity>()
}