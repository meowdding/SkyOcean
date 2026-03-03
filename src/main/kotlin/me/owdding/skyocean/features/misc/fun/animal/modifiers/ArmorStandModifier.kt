package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand

@RegisterAnimalModifier
object ArmorStandModifier : AnimalModifier<ArmorStand, ArmorStandRenderState> {
    override val type: EntityType<ArmorStand> = EntityType.ARMOR_STAND

    override fun apply(
        avatarState: AvatarRenderState,
        state: ArmorStandRenderState,
        partialTicks: Float,
    ) {
        state.isSmall = PlayerAnimalConfig.isBaby.select(avatarState)
    }
}
