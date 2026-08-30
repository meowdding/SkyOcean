package me.owdding.skyocean.features.item

//? >= 26.2 {
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BindGroupLayout
import net.minecraft.client.renderer.BindGroupLayouts
//? } else 26.1 {
/*import com.mojang.blaze3d.vertex.VertexFormat
*///? }
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import me.owdding.ktmodules.Module
import me.owdding.lib.accessor.RenderPipelineBuilderAccessor
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.config.features.inventory.RarityOutlinesConfig
import me.owdding.skyocean.utils.extensions.getRealRarity
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.state.gui.GuiItemRenderState
import net.minecraft.util.ARGB
import net.minecraft.util.Util
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.lwjgl.system.MemoryStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import java.util.*
import java.util.function.BiFunction

/*
Maybe add Leather color as outline color sometime
 */
@Module
object RarityOutlines {
    fun RenderPipeline.Builder.withShaderDefineColor(name: String, color: Int): RenderPipeline.Builder {
        val accessor = this as RenderPipelineBuilderAccessor
        accessor.`meowddinglib$define`(name, "vec4(${ARGB.redFloat(color)}, ${ARGB.greenFloat(color)}, ${ARGB.blueFloat(color)}, ${ARGB.alphaFloat(color)})")
        return this
    }

    @JvmStatic
    fun createPipeline(item: GuiItemRenderState, base: RenderPipeline): RenderPipeline {
        if (!RarityOutlinesConfig.enabled) return base
        val rarity = item.itemStackRenderState().getData(RARITY) ?: return base
        val baseRarity = item.itemStackRenderState().getData(BASE_RARITY).asOptionalInt()

        return GUI_TEXTURED_PREMULTIPLIED_ALPHA_OUTLINED.apply(rarity, baseRarity)
    }

    fun Int?.asOptionalInt(): OptionalInt = OptionalInt.of(this ?: return OptionalInt.empty())

    @JvmStatic
    fun attachData(output: ItemStackRenderState, item: ItemStack) {
        val modifiedRarity = RarityOutlinesConfig.color(item.getData(DataTypes.RARITY))
        val baseRarity = RarityOutlinesConfig.color(item.getRealRarity())
        modifiedRarity?.let { output.setData(RARITY, it) }

        if (RarityOutlinesConfig.baseRarityGlint && baseRarity != modifiedRarity) {
            modifiedRarity?.let { output.setData(BASE_RARITY, it) }
        }
    }


    //? if >= 26.2 {

    private val RARITY_BIND_GROUP: BindGroupLayout = BindGroupLayout.builder()
        .withSampler("Sampler0")
        .withUniform(Buffer.NAME, UniformType.UNIFORM_BUFFER)
        .build()

    @JvmField
    val GUI_TEXTURED_PREMULTIPLIED_ALPHA_OUTLINED: BiFunction<Int, OptionalInt, RenderPipeline> = Util.memoize { color, baseRarity ->
        RenderPipelines.register(
            RenderPipeline.builder()
                .withLocation(id("rarity_outlines/$color"))
                .withVertexShader(id("core/rarity_outlines"))
                .withFragmentShader(id("core/rarity_outlines"))
                .withShaderDefineColor("RARITY_COLOR", color)
                .apply {
                    if (baseRarity.isPresent) {
                        withShaderDefine("IS_RARITY_UPGRADE").withShaderDefineColor("BASE_RARITY_COLOR", baseRarity.asInt)
                    }
                }
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(RARITY_BIND_GROUP)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .build(),
        )
    }

    //? } else {
    
    /*@JvmField
    val GUI_TEXTURED_PREMULTIPLIED_ALPHA_OUTLINED: BiFunction<Int, OptionalInt, RenderPipeline> = Util.memoize { color, baseRarity ->
        RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(id("rarity_outlines/$color"))
                .withVertexShader(id("core/rarity_outlines"))
                .withFragmentShader(id("core/rarity_outlines"))
                .withShaderDefineColor("RARITY_COLOR", color)
                .apply {
                    if (baseRarity.isPresent) {
                        withShaderDefine("IS_RARITY_UPGRADE").withShaderDefineColor("BASE_RARITY_COLOR", baseRarity.asInt)
                    }
                }
                .withSampler("Sampler0")
                .withUniform(Buffer.NAME, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .build()
        )
    }

    *///? }


    @JvmField
    val RARITY: RenderStateDataKey<Int> = RenderStateDataKey.create { id("rarity").toString() }

    @JvmField
    val BASE_RARITY: RenderStateDataKey<Int> = RenderStateDataKey.create { id("base_rarity").toString() }

    object Buffer {
        /*

        float AtlasDimensions;
        float SlotSize;
        float SampleAmount;
        float SampleDistance;
        float AlphaCutoff;
        int GuiScale;

         */
        const val NAME = "SkyoceanRarityUniform"

        @JvmStatic
        private val UBO_SIZE = Std140SizeCalculator()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .putInt()
            .putInt()
            .get()

        private var buffer: GpuBuffer? = null

        fun update(width: Int, slotSize: Int, guiScale: Int) {
            val currentBuffer = buffer
            if (currentBuffer == null || currentBuffer.isClosed) buffer = RenderSystem.getDevice().createBuffer(
                { "SkyOcean Rarity UBO" },
                GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
                UBO_SIZE.toLong(),
            )
            MemoryStack.stackPush().use {
                val data = Std140Builder.onStack(it, UBO_SIZE)
                    .putFloat(width.toFloat())
                    .putFloat(slotSize.toFloat())
                    .putFloat(RarityOutlinesConfig.sampleAmount)
                    .putFloat(RarityOutlinesConfig.sampleDistance)
                    .putFloat(RarityOutlinesConfig.alphaCutoff)
                    .putFloat(RarityOutlinesConfig.outlineAlpha)
                    .putInt(RarityOutlinesConfig.kernelType)
                    .putInt(guiScale)
                    .get()
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer!!.slice(), data)
            }
        }

        @JvmStatic
        fun getGpuBuffer(): GpuBuffer? = buffer
    }
}
