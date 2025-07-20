package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderType

object RenderTypes {

    val BLOCK_FILL: RenderType = RenderType.create(
        "skyocean/depth_block_fill/quad",
        131072,
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/debug_filled_box")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withDepthBias(-1f, -10f)
            .build(),
        RenderType.CompositeState.builder()
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false),
    )

    val BLOCK_FILL_TRIANGLE_THROUGH_WALLS: RenderType = RenderType.create(
        "skyocean/depth_block_fill/triangle/through_walls",
        131072,
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/debug_filled_box")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthBias(-1f, -10f)
            .build(),
        RenderType.CompositeState.builder()
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false),
    )

}
