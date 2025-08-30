package me.owdding.skyocean.utils.rendering

import net.minecraft.resources.ResourceLocation

interface PostEffectApplicator {
    fun `skyocean$applyPostEffect`(id: ResourceLocation)

    fun `skyocean$getPostEffect`(): ResourceLocation?
}
