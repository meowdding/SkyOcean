package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.EntityTypes
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.block.BlockModelResolver
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.BoggedRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.skeleton.Bogged

@RegisterAnimalModifier
object BoggedModifier : AnimalModifier<Bogged, BoggedRenderState> {
    override val type: EntityType<Bogged> = EntityTypes.BOGGED

    var isSheared = PlayerAnimalConfig.createEntry("bogged_sheared") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("bogged", "${type}_sheared")
            condition = isSelected(EntityTypes.BOGGED)
        }
    }

    override fun apply(
        resolver: BlockModelResolver,
        avatarState: AvatarRenderState,
        state: BoggedRenderState,
        partialTicks: Float,
    ) {
        state.isSheared = isSheared.select(avatarState).select(avatarState)
    }
}
