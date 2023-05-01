package mash.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.utils.RenderContext
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.assets.toInternalFile


class TestShader : Shader {
    private lateinit var context: RenderContext
    private lateinit var camera: Camera
    private lateinit var program: ShaderProgram
    override fun dispose() {
        program.dispose()
    }

    override fun init() {

        val vert = "data/test.vertext.glsl".toInternalFile().readString()
        val frag = "data/test.fragment.glsl".toInternalFile().readString()
        program = ShaderProgram(vert, frag)
        if (!program.isCompiled) throw GdxRuntimeException(program.log)
    }

    override fun compareTo(other: Shader): Int {
        return 0
    }

    override fun canRender(instance: Renderable): Boolean {
        return true
    }

    override fun begin(camera: Camera, context: RenderContext) {
        this.camera = camera
        this.context = context
        program.bind()
        program.setUniformMatrix("u_projViewTrans", camera.combined)
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    override fun render(renderable: Renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform)
        renderable.meshPart.render(program)
    }

    override fun end() {
    }
}