package mash.shaders

import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider

class CustomShaderProvider(config: PBRShaderConfig) : PBRShaderProvider(config) {

    override fun createShader(renderable: Renderable): Shader {
        if (renderable.material.has(ToonShaderFlag.ToonShaderFlag)) {
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
            "shaders/modified_default/gdx-pbr.vs.glsl",
            "shaders/modified_default/gdx-pbr.fs.glsl"
        )
    }
}

