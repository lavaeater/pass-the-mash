package mash.injection

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import eater.injection.InjectionContext.Companion.inject
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import net.mgsx.gltf.loaders.gltf.GLTFLoader

fun assets(): Assets {
    return inject()
}

class Assets() : DisposableRegistry by DisposableContainer() {

    val coralTexture = Texture("coral.png".toInternalFile())
    val submarine by lazy { GLTFLoader().load(Gdx.files.internal("models/submarine3.gltf")) }

    override fun dispose() {
        registeredDisposables.disposeSafely()
    }
}
