package me.owdding.skyocean.features.misc.`fun`.animal

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.FoxRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Fox

@RegisterAnimalModifier
object FoxModifier : AnimalModifier<Fox, FoxRenderState> {
    override val type: EntityType<Fox> = EntityType.FOX
    private val foxVariants = Fox.Variant.entries

    var foxVariant = PlayerAnimalConfig.createEntry("fox_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = "skyocean.config.misc.fun.player_animals.cat.${type}_variant"
            condition = isSelected(EntityType.FOX)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: FoxRenderState,
        partialTicks: Float,
    ) {
        state.variant = foxVariant.select(avatarState).variant ?: getRandom(avatarState, foxVariants)
        state.isSitting = avatarState.isCrouching
    }

    enum class Variant(val variant: Fox.Variant?) : Translatable {
        RANDOM(null),
        RED(Fox.Variant.RED),
        SNOW(Fox.Variant.SNOW),
        ;

        override fun getTranslationKey(): String = "$name"
    }
}
