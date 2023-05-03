package mash.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.assets.toInternalFile
import net.mgsx.gltf.scene3d.attributes.*
import net.mgsx.gltf.scene3d.shaders.PBRCommon
import net.mgsx.gltf.scene3d.shaders.PBRShader
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import net.mgsx.gltf.scene3d.utils.LightUtils
import net.mgsx.gltf.scene3d.utils.LightUtils.LightsInfo

class CustomShaderProvider(config: PBRShaderConfig) : PBRShaderProvider(config) {
    val defaultShaderProviderConfig = DefaultShader.Config().apply {
        this.numBones = 60
    }

    val defaultShaderProvider = DefaultShaderProvider(defaultShaderProviderConfig)


    fun useToonShader(renderable: Renderable): Boolean {
        return (
                renderable
                    .material
                    .has(ColorAttribute.Fog) && (renderable
                    .material
                    .get(ColorAttribute.Fog) as ColorAttribute)
                    .color == Color.BLACK
                ) ||
                renderable
                    .material
                    .has(PBRColorAttribute.Fog)
    }

    override fun createShader(renderable: Renderable): Shader {
        return if (useToonShader(renderable))
            createToonShader(renderable)
        else
            super.createShader(renderable)
    }

    fun createPrefix(renderable: Renderable, config: PBRShaderConfig): String {

        var prefix = createPrefixBase(renderable, config)

        // Morph targets

        // Morph targets
        prefix += morphTargetsPrefix(renderable)

        // optional base color factor

        // optional base color factor
        if (renderable.material.has(PBRColorAttribute.BaseColorFactor)) {
            prefix += "#define baseColorFactorFlag\n"
        }

        // Lighting

        // Lighting
        val primitiveType = renderable.meshPart.primitiveType
        val isLineOrPoint =
            primitiveType == GL20.GL_POINTS || primitiveType == GL20.GL_LINES || primitiveType == GL20.GL_LINE_LOOP || primitiveType == GL20.GL_LINE_STRIP
        val unlit =
            isLineOrPoint || renderable.material.has(PBRFlagAttribute.Unlit) || renderable.meshPart.mesh.getVertexAttribute(
                VertexAttributes.Usage.Normal
            ) == null

        if (unlit) {
            prefix += "#define unlitFlag\n"
        } else {
            if (renderable.material.has(PBRTextureAttribute.MetallicRoughnessTexture)) {
                prefix += "#define metallicRoughnessTextureFlag\n"
            }
            if (renderable.material.has(PBRTextureAttribute.OcclusionTexture)) {
                prefix += "#define occlusionTextureFlag\n"
            }

            // IBL options
            var specualarCubemapAttribute: PBRCubemapAttribute? = null
            if (renderable.environment != null) {
                if (renderable.environment.has(PBRCubemapAttribute.SpecularEnv)) {
                    prefix += "#define diffuseSpecularEnvSeparateFlag\n"
                    specualarCubemapAttribute =
                        renderable.environment.get(PBRCubemapAttribute::class.java, PBRCubemapAttribute.SpecularEnv)
                } else if (renderable.environment.has(PBRCubemapAttribute.DiffuseEnv)) {
                    specualarCubemapAttribute =
                        renderable.environment.get(PBRCubemapAttribute::class.java, PBRCubemapAttribute.DiffuseEnv)
                } else if (renderable.environment.has(PBRCubemapAttribute.EnvironmentMap)) {
                    specualarCubemapAttribute =
                        renderable.environment.get(PBRCubemapAttribute::class.java, PBRCubemapAttribute.EnvironmentMap)
                }
                if (specualarCubemapAttribute != null) {
                    prefix += "#define USE_IBL\n"
                    val textureLodSupported: Boolean
                    if (isGL3) {
                        textureLodSupported = true
                    } else if (Gdx.graphics.supportsExtension("EXT_shader_texture_lod")) {
                        prefix += "#define USE_TEXTURE_LOD_EXT\n"
                        textureLodSupported = true
                    } else {
                        textureLodSupported = false
                    }
                    val textureFilter =
                        if (specualarCubemapAttribute.textureDescription.minFilter != null) specualarCubemapAttribute.textureDescription.minFilter else specualarCubemapAttribute.textureDescription.texture.minFilter
                    if (textureLodSupported && textureFilter == TextureFilter.MipMap) {
                        prefix += "#define USE_TEX_LOD\n"
                    }
                    if (renderable.environment.has(PBRTextureAttribute.BRDFLUTTexture)) {
                        prefix += "#define brdfLUTTexture\n"
                    }
                }
                // TODO check GLSL extension 'OES_standard_derivatives' for WebGL
                if (renderable.environment.has(ColorAttribute.AmbientLight)) {
                    prefix += "#define ambientLightFlag\n"
                }
                if (renderable.environment.has(PBRMatrixAttribute.EnvRotation)) {
                    prefix += "#define ENV_ROTATION\n"
                }
            }
        }

        // SRGB

        // SRGB
        prefix += createPrefixSRGB(renderable, config)


        // multi UVs


        // multi UVs
        var maxUVIndex = -1

            var attribute = renderable.material.get(TextureAttribute::class.java, TextureAttribute.Diffuse)
            if (attribute != null) {
                prefix += "#define v_diffuseUV v_texCoord" + attribute.uvIndex + "\n"
                maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex)
            }
            attribute = renderable.material.get(TextureAttribute::class.java, TextureAttribute.Emissive)
            if (attribute != null) {
                prefix += "#define v_emissiveUV v_texCoord" + attribute.uvIndex + "\n"
                maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex)
            }
            attribute = renderable.material.get(TextureAttribute::class.java, TextureAttribute.Normal)
            if (attribute != null) {
                prefix += "#define v_normalUV v_texCoord" + attribute.uvIndex + "\n"
                maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex)
            }
            attribute =
                renderable.material.get(TextureAttribute::class.java, PBRTextureAttribute.MetallicRoughnessTexture)
            if (attribute != null) {
                prefix += "#define v_metallicRoughnessUV v_texCoord" + attribute.uvIndex + "\n"
                maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex)
            }
            attribute = renderable.material.get(TextureAttribute::class.java, PBRTextureAttribute.OcclusionTexture)
            if (attribute != null) {
                prefix += "#define v_occlusionUV v_texCoord" + attribute.uvIndex + "\n"
                maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex)
            }

        if (maxUVIndex >= 0) {
            prefix += "#define textureFlag\n"
        }
        if (maxUVIndex == 1) {
            prefix += "#define textureCoord1Flag\n"
        } else if (maxUVIndex > 1) {
            throw GdxRuntimeException("more than 2 texture coordinates attribute not supported")
        }

        // Fog


        // Fog
        if (renderable.environment != null && renderable.environment.has(FogAttribute.FogEquation)) {
            prefix += "#define fogEquationFlag\n"
        }


        // colors


        // colors
        for (attribute in renderable.meshPart.mesh.vertexAttributes) {
            if (attribute.usage == VertexAttributes.Usage.ColorUnpacked) {
                prefix += "#define color" + attribute.unit + "Flag\n"
            }
        }

        //


        //
        var numBoneInfluence = 0
        var numMorphTarget = 0
        var numColor = 0

        for (attribute in renderable.meshPart.mesh.vertexAttributes) {
            if (attribute.usage == VertexAttributes.Usage.ColorPacked) {
                throw GdxRuntimeException("color packed attribute not supported")
            } else if (attribute.usage == VertexAttributes.Usage.ColorUnpacked) {
                numColor = Math.max(numColor, attribute.unit + 1)
            } else if (attribute.usage == PBRVertexAttributes.Usage.PositionTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS || attribute.usage == PBRVertexAttributes.Usage.NormalTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS || attribute.usage == PBRVertexAttributes.Usage.TangentTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS) {
                numMorphTarget = Math.max(numMorphTarget, attribute.unit + 1)
            } else if (attribute.usage == VertexAttributes.Usage.BoneWeight) {
                numBoneInfluence = Math.max(numBoneInfluence, attribute.unit + 1)
            }
        }


        PBRCommon.checkVertexAttributes(renderable)

        if (numBoneInfluence > 8) {
            Gdx.app.error(
                TAG,
                "more than 8 bones influence attributes not supported: $numBoneInfluence found."
            )
        }
        if (numMorphTarget > PBRCommon.MAX_MORPH_TARGETS) {
            Gdx.app.error(TAG, "more than 8 morph target attributes not supported: $numMorphTarget found.")
        }
        if (numColor > config.numVertexColors) {
            Gdx.app.error(
                TAG,
                "more than " + config.numVertexColors + " color attributes not supported: " + numColor + " found."
            )
        }

        if (renderable.environment != null) {
            LightUtils.getLightsInfo(lightsInfo, renderable.environment)
            if (lightsInfo.dirLights > config.numDirectionalLights) {
                Gdx.app.error(
                    TAG,
                    "too many directional lights detected: " + lightsInfo.dirLights + "/" + config.numDirectionalLights
                )
            }
            if (lightsInfo.pointLights > config.numPointLights) {
                Gdx.app.error(
                    TAG,
                    "too many point lights detected: " + lightsInfo.pointLights + "/" + config.numPointLights
                )
            }
            if (lightsInfo.spotLights > config.numSpotLights) {
                Gdx.app.error(
                    TAG,
                    "too many spot lights detected: " + lightsInfo.spotLights + "/" + config.numSpotLights
                )
            }
            if (lightsInfo.miscLights > 0) {
                Gdx.app.error(TAG, "unknow type lights not supported.")
            }
        }
        return prefix
    }

    private val toonShaderConfig = PBRShaderConfig().apply {
        numBones = 60
        vertexShader = "shaders/default/gdx-pbr.vs.glsl".toInternalFile().readString()
        fragmentShader = "shaders/default/gdx-pbr.fs.glsl".toInternalFile().readString()
    }

    private fun createToonShader(
        renderable: Renderable
    ): Shader {
        return TestShader(
            renderable,
            toonShaderConfig,
            createPrefix(renderable, toonShaderConfig))

//        return DefaultShader(renderable, config, DefaultShader.createPrefix(renderable, defaultShaderProviderConfig))

//
//        return DefaultShader(renderable, config)
//        return TestShader(
//            renderable,
//            config,
//            "shaders/default/gdx-pbr.vs.glsl",
//            "shaders/default/gdx-pbr.fs.glsl"
//        )
    }

    companion object {

        val lightsInfo = LightsInfo()
    }
}

