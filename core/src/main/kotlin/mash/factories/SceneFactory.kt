package mash.factories

import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.disposeSafely
import net.mgsx.gltf.scene3d.scene.SceneManager

abstract class SceneFactory(): DisposableRegistry by DisposableContainer() {

    abstract fun loadScene(sceneManager: SceneManager)
}

class LoadCarFactory :SceneFactory() {
    override fun loadScene(sceneManager: SceneManager) {
        /**
         * Is it reasonable to just load resources here that we just might only need
         * for this particular scene?
         */
    }

    override fun dispose() {
        super.dispose()
        registeredDisposables.disposeSafely()
    }
}