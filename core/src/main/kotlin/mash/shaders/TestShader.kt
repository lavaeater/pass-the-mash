package mash.shaders

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.Attributes
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.RenderContext
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.assets.toInternalFile
import net.mgsx.gltf.scene3d.shaders.PBRShader


class TestShader(
    renderable: Renderable,
    config: Config,
    prefix: String
) : PBRShader(
    renderable,
    config,prefix
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun equals(obj: DefaultShader?): Boolean {
        return super.equals(obj)
    }

    override fun dispose() {
        super.dispose()
    }

    override fun init() {
        super.init()
    }

    override fun init(program: ShaderProgram?, renderable: Renderable?) {
        super.init(program, renderable)
    }

    override fun compareTo(other: Shader?): Int {
        return super.compareTo(other)
    }

    override fun begin(camera: Camera?, context: RenderContext?) {
        super.begin(camera, context)
    }

    override fun render(renderable: Renderable?, combinedAttributes: Attributes?) {
        super.render(renderable, combinedAttributes)
    }

    override fun render(renderable: Renderable?) {
        super.render(renderable)
    }

    override fun end() {
        super.end()
    }

    override fun register(alias: String?, validator: Validator?, setter: Setter?): Int {
        return super.register(alias, validator, setter)
    }

    override fun register(alias: String?, validator: Validator?): Int {
        return super.register(alias, validator)
    }

    override fun register(alias: String?, setter: Setter?): Int {
        return super.register(alias, setter)
    }

    override fun register(alias: String?): Int {
        return super.register(alias)
    }

    override fun register(uniform: Uniform?, setter: Setter?): Int {
        return super.register(uniform, setter)
    }

    override fun register(uniform: Uniform?): Int {
        return super.register(uniform)
    }

    override fun getUniformID(alias: String?): Int {
        return super.getUniformID(alias)
    }

    override fun getUniformAlias(id: Int): String {
        return super.getUniformAlias(id)
    }

    override fun bindMaterial(attributes: Attributes?) {
        super.bindMaterial(attributes)
    }

    override fun bindLights(renderable: Renderable?, attributes: Attributes?) {
        super.bindLights(renderable, attributes)
    }

    override fun getDefaultCullFace(): Int {
        return super.getDefaultCullFace()
    }

    override fun setDefaultCullFace(cullFace: Int) {
        super.setDefaultCullFace(cullFace)
    }

    override fun getDefaultDepthFunc(): Int {
        return super.getDefaultDepthFunc()
    }

    override fun setDefaultDepthFunc(depthFunc: Int) {
        super.setDefaultDepthFunc(depthFunc)
    }

    //    override fun init() {
//
//        val vert = "shaders/default/gdx-pbr.vs.glsl".toInternalFile().readString()
//        val frag = "shaders/default/gdx-pbr.fs.glsl".toInternalFile().readString()
//        program = ShaderProgram(vert, frag)
//        if (!program.isCompiled) throw GdxRuntimeException(program.log)
//    }

    override fun canRender(instance: Renderable): Boolean {
        return true
    }




//
//    override fun end() {
//    }
}