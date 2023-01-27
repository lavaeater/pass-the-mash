package mash.factories

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import net.mgsx.gltf.scene3d.scene.Scene

class TrackGenerator {
    fun generateTrack(): MashTrack {

        /*

        How does one generate a track? We want the tracks to be self-connective.

        GET BACK TO WORK YOU FOOL!
         */


        return MashTrack()
    }

}

class MashTrack {
    lateinit var scene: Scene
    lateinit var body: btRigidBody

}
