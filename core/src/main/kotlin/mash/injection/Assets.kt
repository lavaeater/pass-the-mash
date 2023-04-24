package mash.injection

import twodee.injection.InjectionContext.Companion.inject
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.disposeSafely

fun assets(): Assets {
    return inject()
}

class Assets : DisposableRegistry by DisposableContainer() {

    override fun dispose() {
        registeredDisposables.disposeSafely()
    }
}
