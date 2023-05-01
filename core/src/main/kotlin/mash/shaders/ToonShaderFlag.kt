package mash.shaders

import com.badlogic.gdx.graphics.g3d.Attribute

class ToonShaderFlag(type: Long) : Attribute(type) {

    override fun copy(): Attribute {
        return ToonShaderFlag(type)
    }

    override fun compareTo(other: Attribute): Int {
        return (type - other.type).toInt()
    }

    companion object {
        val ToonShaderFlagAlias = "toonshaderflag"
        val ToonShaderFlag = register(ToonShaderFlagAlias)
    }

}