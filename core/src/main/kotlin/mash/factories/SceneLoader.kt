package mash.factories

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import net.mgsx.gltf.scene3d.scene.SceneManager


abstract class SceneLoader : DisposableRegistry by DisposableContainer() {

    abstract fun loadScene(sceneManager: SceneManager, dynamicsWorld: btDynamicsWorld)
    protected fun setUpScene(sceneManager: SceneManager) {
        setupEnvironment(sceneManager)
    }

    abstract fun setupEnvironment(sceneManager: SceneManager)
}

