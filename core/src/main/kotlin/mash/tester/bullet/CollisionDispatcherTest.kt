//package mash.tester.bullet
//
//import com.badlogic.gdx.math.MathUtils
//import com.badlogic.gdx.physics.bullet.collision.*
//import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
//import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
//
//class CollisionDispatcherTest : BaseBulletTest() {
//    class MyCollisionDispatcher(collisionConfiguration: btCollisionConfiguration?) :
//        CustomCollisionDispatcher(collisionConfiguration) {
//        override fun needsCollision(body0: btCollisionObject, body1: btCollisionObject): Boolean {
//            return if (body0.userValue % 2 == 0 || body1.userValue % 2 == 0) super.needsCollision(
//                body0,
//                body1
//            ) else false
//        }
//
//        override fun needsResponse(body0: btCollisionObject, body1: btCollisionObject): Boolean {
//            return if (body0.userValue % 2 == 0 || body1.userValue % 2 == 0) super.needsCollision(
//                body0,
//                body1
//            ) else false
//        }
//    }
//
//    override fun createWorld(): BulletWorld? {
//        val collisionConfiguration = btDefaultCollisionConfiguration()
//        val dispatcher = MyCollisionDispatcher(collisionConfiguration)
//        val broadphase = btDbvtBroadphase()
//        val solver = btSequentialImpulseConstraintSolver()
//        val collisionWorld = btDiscreteDynamicsWorld(
//            dispatcher, broadphase, solver,
//            collisionConfiguration
//        )
//        return BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, collisionWorld)
//    }
//
//    override fun create() {
//        super.create()
//
//        // Create the entities
//        world!!.add("ground", 0f, 0f, 0f)!!
//            .setColor(
//                0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(),
//                0.25f + 0.5f * Math.random().toFloat(), 1f
//            )
//        var x = -5f
//        while (x <= 5f) {
//            var y = 5f
//            while (y <= 15f) {
//                world!!.add(
//                    "box",
//                    x + 0.1f * MathUtils.random(),
//                    y + 0.1f * MathUtils.random(),
//                    0.1f * MathUtils.random()
//                )!!.body.userValue = ((x + 5f) / 2f + .5f).toInt()
//                y += 2f
//            }
//            x += 2f
//        }
//    }
//
//    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
//        shoot(x, y)
//        return true
//    }
//}