package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.SalmonRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Salmon

@RegisterAnimalModifier
object SalmonModifier : AnimalModifier<Salmon, SalmonRenderState> {
    override val type: EntityType<Salmon> = EntityType.SALMON
    private val salmonVariants = Salmon.Variant.entries

    var salmonVariant = PlayerAnimalConfig.createEntry("salmon_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("salmon", "${type}_variant")
            condition = isSelected(EntityType.SALMON)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: SalmonRenderState,
        partialTicks: Float,
    ) {
        state.variant = salmonVariant.select(avatarState).variant ?: getRandom(avatarState, salmonVariants)
    }

    enum class Variant(val variant: Salmon.Variant?) : Translatable {
        RANDOM(null),

        SMALL(Salmon.Variant.SMALL),
        MEDIUM(Salmon.Variant.MEDIUM),
        LARGE(Salmon.Variant.LARGE),
        ;

        override fun getTranslationKey(): String = createTranslationKey("salmon", "variant", name)
    }
}
