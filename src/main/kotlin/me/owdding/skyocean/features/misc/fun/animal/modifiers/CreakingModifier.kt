package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CreakingRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.creaking.Creaking

@RegisterAnimalModifier
object CreakingModifier : AnimalModifier<Creaking, CreakingRenderState> {
    override val type: EntityType<Creaking> = EntityType.CREAKING

    override fun apply(
        avatarState: AvatarRenderState,
        state: CreakingRenderState,
        partialTicks: Float,
    ) {
        state.canMove = true
    }
}
