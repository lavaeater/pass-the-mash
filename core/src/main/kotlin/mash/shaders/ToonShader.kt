package mash.shaders

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.RenderContext

class ToonShader(
    private val renderable: Renderable,
    config: DefaultShader.Config
) : Shader {
    val prefix = DefaultShader.createPrefix(renderable, config)

    val attributesMask: Long = combineAttributeMasks(renderable)

    private fun combineAttributeMasks(renderable: Renderable): Long {
        var mask: Long = 0
        if (renderable.environment != null) mask = mask or renderable.environment.mask
        if (renderable.material != null) mask = mask or renderable.material.mask
        return mask
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: Shader?): Int {
        TODO("Not yet implemented")
    }

    override fun canRender(instance: Renderable?): Boolean {
        TODO("Not yet implemented")
    }

    override fun begin(camera: Camera?, context: RenderContext?) {
        TODO("Not yet implemented")
    }

    override fun render(renderable: Renderable?) {
        TODO("Not yet implemented")
    }

    override fun end() {
        TODO("Not yet implemented")
    }

}