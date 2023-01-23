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
//import com.badlogic.gdx.graphics.Color
//import com.badlogic.gdx.physics.bullet.dynamics.*
//
///** @author jsjolund, ax-rwnd and mjolnir92
// */
//class VehicleFilterTest : VehicleTest() {
//    override fun getRaycaster(): btVehicleRaycaster {
//        val raycaster = FilterableVehicleRaycaster(world!!.collisionWorld as btDynamicsWorld)
//        raycaster.setCollisionFilterGroup(FILTER_GROUP)
//        raycaster.setCollisionFilterMask(FILTER_MASK)
//        return raycaster
//    }
//
//    override fun create() {
//        super.create()
//        chassis.color = Color.BLUE
//    }
//
//    override fun createWorld(): BulletWorld {
//        // Force all objects to same collision group and filter
//        return object : BulletWorld() {
//            override fun add(entity: BulletEntity) {
//                world!!.entities.add(entity)
//                if (entity!!.body != null) {
//                    if (entity.body is btRigidBody) (collisionWorld as btDiscreteDynamicsWorld).addRigidBody(
//                        entity.body as btRigidBody,
//                        FILTER_GROUP.toInt(),
//                        FILTER_MASK.toInt()
//                    ) else collisionWorld.addCollisionObject(
//                        entity.body, FILTER_GROUP.toInt(), FILTER_MASK.toInt()
//                    )
//                    // Store the index of the entity in the collision object.
//                    entity.body!!.userValue = entities.size - 1
//                }
//            }
//        }
//    }
//
//    companion object {
//        const val FILTER_GROUP = (1 shl 11).toShort()
//        const val FILTER_MASK = FILTER_GROUP
//    }
//}