package mash.shaders

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider

class CustomShaderProvider(config: PBRShaderConfig) : PBRShaderProvider(config) {

    override fun createShader(renderable: Renderable): Shader {
        if (renderable.material.has(ColorAttribute.Fog) && (renderable.material.get(ColorAttribute.Fog) as ColorAttribute).color == Color.BLACK) {
            return createToonShader(config, renderable)
        }
        return super.createShader(renderable)
    }

    private fun createToonShader(
        config: DefaultShader.Config,
        renderable: Renderable
    ): Shader {
        return TestShader(
            renderable,
            config,
            "shaders/default/gdx-pbr.vs.glsl",
            "shaders/default/gdx-pbr.fs.glsl"
        )
    }
}

