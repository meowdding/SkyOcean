package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.SnowGolem

@RegisterAnimalModifier
object SnowGolemModifier : AnimalModifier<SnowGolem, SnowGolemRenderState> {
    override val type: EntityType<SnowGolem> = EntityType.SNOW_GOLEM

    var snowGolemPumpkin = PlayerAnimalConfig.createEntry("snow_golem_pumpkin") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("snow_golem", "${type}_pumpkin")
            condition = isSelected(EntityType.SNOW_GOLEM)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: SnowGolemRenderState,
        partialTicks: Float,
    ) {
        state.hasPumpkin = snowGolemPumpkin.select(avatarState).select(avatarState)
    }
}
