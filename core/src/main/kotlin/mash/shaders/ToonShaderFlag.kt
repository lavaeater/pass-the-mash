package mash.shaders

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute


class CustomColorTypes : ColorAttribute(0) {

    companion object {
        const val ToonColorAlias = "ToonColor" // step 1: name the type
        val ToonColor: Long = register(ToonColorAlias) // step 2: register the type

        init {
            Mask = Mask or ToonColor // step 3: Make ColorAttribute accept the type
        }
    }
}


