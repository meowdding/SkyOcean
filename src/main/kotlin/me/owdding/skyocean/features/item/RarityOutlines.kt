package me.owdding.skyocean.features.item


import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import me.owdding.ktmodules.Module
import me.owdding.lib.accessor.RenderPipelineBuilderAccessor
import me.owdding.skyocean.SkyOcean.id
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.ARGB
import net.minecraft.util.Util
import org.lwjgl.system.MemoryStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import java.util.function.Function

/*
New/Old colors
Outline Thickness
Sample Size
Outline alpha override

 */
@Module
object RarityOutlines {
    fun RenderPipeline.Builder.withShaderDefineColor(name: String, color: Int): RenderPipeline.Builder {
        val accessor = this as RenderPipelineBuilderAccessor
        accessor.`meowddinglib$define`(name, "vec4(${ARGB.redFloat(color)}, ${ARGB.greenFloat(color)}, ${ARGB.blueFloat(color)}, ${ARGB.alphaFloat(color)})")
        return this
    }


    //? if >= 26.2 {

    private val RARITY_BIND_GROUP: BindGroupLayout = BindGroupLayout.builder()
        .withSampler("Sampler0")
        .withUniform(Buffer.NAME, UniformType.UNIFORM_BUFFER)
        .build()

    @JvmField
    val GUI_TEXTURED_PREMULTIPLIED_ALPHA_OUTLINED: Function<Int, RenderPipeline> = Util.memoize { color ->
        RenderPipelines.register(
            RenderPipeline.builder()
                .withLocation(id("rarity_outlines/$color"))
                .withVertexShader(id("core/rarity_outlines"))
                .withFragmentShader(id("core/rarity_outlines"))
                .withShaderDefineColor("RARITY_COLOR", color)
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(RARITY_BIND_GROUP)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .build()
        )
    }

    //? } else {
    /*
    @JvmField
    val GUI_TEXTURED_PREMULTIPLIED_ALPHA_OUTLINED: BiFunction<Int, Vector4f, RenderPipeline> = Util.memoize { color, uvs ->
        RenderPipelines.register(
            RenderPipeline.builder()
                .withLocation(id("rarity_outlines/$color"))
                .withVertexShader(id("core/rarity_outlines"))
                .withFragmentShader(id("core/rarity_outlines"))
                .withShaderDefineColor("RARITY_COLOR", color)
                .withShaderDefine("MIN_UV", Vector2f(uvs.x, uvs.y))
                .withShaderDefine("MAX_UV", Vector2f(uvs.z, uvs.w))
                .withSampler("Sampler0")
                .withUniform(Buffer.NAME, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .build()
        )
    }

    *///? }


    @JvmField
    val RARITY: RenderStateDataKey<SkyBlockRarity> = RenderStateDataKey.create { id("rarity").toString() }

    object Buffer {
        const val NAME = "SkyoceanRarityUniform"

        @JvmStatic
        private val UBO_SIZE = Std140SizeCalculator().putFloat().putFloat().putInt().get()

        private var buffer: GpuBuffer? = null

        fun update(width: Int, slotSize: Int, guiScale: Int) {
            if (buffer == null) buffer = RenderSystem.getDevice().createBuffer(
                { "SkyOcean Rarity UBO" },
                GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
                UBO_SIZE.toLong(),
            )
            MemoryStack.stackPush().use {
                val data = Std140Builder.onStack(it, UBO_SIZE).putFloat(width.toFloat()).putFloat(slotSize.toFloat()).putInt(guiScale).get()
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer!!.slice(), data)
            }
        }

        @JvmStatic
        fun getGpuBuffer(): GpuBuffer? = buffer
    }
}
