package mash.tester.bullet

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver

class CharacterTest : BaseBulletTest() {
    val BOXCOUNT_X = 5
    val BOXCOUNT_Y = 5
    val BOXCOUNT_Z = 1
    val BOXOFFSET_X = -2.5f
    val BOXOFFSET_Y = 0.5f
    val BOXOFFSET_Z = 0f
    var ground: BulletEntity? = null
    var character: BulletEntity? = null
    var ghostPairCallback: btGhostPairCallback? = null
    var ghostObject: btPairCachingGhostObject? = null
    var ghostShape: btConvexShape? = null
    var characterController: btKinematicCharacterController? = null
    var characterTransform: Matrix4? = null
    var characterDirection = Vector3()
    var walkDirection = Vector3()
    override fun createWorld(): BulletWorld {
        // We create the world using an axis sweep broadphase for this test
        val collisionConfiguration = btDefaultCollisionConfiguration()
        val dispatcher = btCollisionDispatcher(collisionConfiguration)
        val sweep = btAxisSweep3(Vector3(-1000f, -1000f, -1000f), Vector3(1000f, 1000f, 1000f))
        val solver = btSequentialImpulseConstraintSolver()
        val collisionWorld = btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration)
        ghostPairCallback = btGhostPairCallback()
        sweep.overlappingPairCache.setInternalGhostPairCallback(ghostPairCallback)
        return BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld)
    }

    override fun create() {
        super.create()
        instructions =
            "Tap to shoot\nArrow keys to move\nR to reset\nLong press to toggle debug mode\nSwipe for next test"

        // Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that
        // manually)
        val texture = Texture(Gdx.files.internal("data/badlogic.jpg"))
        disposables.add(texture)
        val material = Material(
            TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
            FloatAttribute.createShininess(8f)
        )
        val attributes =
            (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong()
        val capsule = modelBuilder.createCapsule(2f, 6f, 16, material, attributes)
        disposables.add(capsule)
        world!!.addConstructor("capsule", BulletConstructor(capsule, null))
        character = world!!.add("capsule", 5f, 3f, 5f)
        characterTransform = character!!.transform // Set by reference
        characterTransform!!.rotate(Vector3.X, 90f)

        // Create the physics representation of the character
        ghostObject = btPairCachingGhostObject()
        ghostObject!!.worldTransform = characterTransform
        ghostShape = btCapsuleShape(2f, 2f)
        ghostObject!!.collisionShape = ghostShape
        ghostObject!!.collisionFlags = btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT
        characterController = btKinematicCharacterController(ghostObject, ghostShape, .35f, Vector3.Y)

        // And add it to the physics world
        world!!.collisionWorld.addCollisionObject(
            ghostObject, btBroadphaseProxy.CollisionFilterGroups.CharacterFilter.toShort().toInt(),
            (btBroadphaseProxy.CollisionFilterGroups.StaticFilter or btBroadphaseProxy.CollisionFilterGroups.DefaultFilter).toShort()
                .toInt()
        )
        (world!!.collisionWorld as btDiscreteDynamicsWorld).addAction(characterController)

        // Add the ground
        world!!.add("ground", 0f, 0f, 0f).also { ground = it }!!.setColor(
            0.25f + 0.5f * Math.random().toFloat(),
            0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 1f
        )
        // Create some boxes to play with
        for (x in 0 until BOXCOUNT_X) {
            for (y in 0 until BOXCOUNT_Y) {
                for (z in 0 until BOXCOUNT_Z) {
                    world!!.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)!!
                        .setColor(
                            0.5f + 0.5f * Math.random().toFloat(),
                            0.5f + 0.5f * Math.random().toFloat(), 0.5f + 0.5f * Math.random().toFloat(), 1f
                        )
                }
            }
        }
    }

    override fun update() {
        // If the left or right key is pressed, rotate the character and update its physics update accordingly.
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            characterTransform!!.rotate(0f, 1f, 0f, 5f)
            ghostObject!!.worldTransform = characterTransform
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            characterTransform!!.rotate(0f, 1f, 0f, -5f)
            ghostObject!!.worldTransform = characterTransform
        }
        // Fetch which direction the character is facing now
        characterDirection.set(-1f, 0f, 0f).rot(characterTransform).nor()
        // Set the walking direction accordingly (either forward or backward)
        walkDirection[0f, 0f] = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) walkDirection.add(characterDirection)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) walkDirection.add(
            -characterDirection.x,
            -characterDirection.y,
            -characterDirection.z
        )
        walkDirection.scl(4f * Gdx.graphics.deltaTime)
        // And update the character controller
        characterController!!.setWalkDirection(walkDirection)
        // Now we can update the world as normally
        super.update()
        // And fetch the new transformation of the character (this will make the model be rendered correctly)
        ghostObject!!.getWorldTransform(characterTransform)
    }

    override fun renderWorld() {
        // TODO Auto-generated method stub
        super.renderWorld()
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        shoot(x, y)
        return true
    }

    override fun dispose() {
        (world!!.collisionWorld as btDiscreteDynamicsWorld).removeAction(characterController)
        world!!.collisionWorld.removeCollisionObject(ghostObject)
        super.dispose()
        characterController!!.dispose()
        ghostObject!!.dispose()
        ghostShape!!.dispose()
        ghostPairCallback!!.dispose()
        ground = null
    }
}