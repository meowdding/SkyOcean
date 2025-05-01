package me.owdding.skyocean.events

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.renderer.MultiBufferSource
import tech.thatgravyboat.skyblockapi.api.events.base.SkyBlockEvent

data class RenderWorldEvent(
    val pose: PoseStack,
    val buffer: MultiBufferSource,
    val camera: Camera,
) : SkyBlockEvent()
