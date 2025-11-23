package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.ParrotRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Parrot

@RegisterAnimalModifier
object ParrotModifier : AnimalModifier<Parrot, ParrotRenderState> {
    override val type: EntityType<Parrot> = EntityType.PARROT
    private val parrotVariants = Parrot.Variant.entries

    var parrotVariant = PlayerAnimalConfig.createEntry("parrot_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("parrot", "${type}_variant")
            condition = isSelected(EntityType.PARROT)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: ParrotRenderState,
        partialTicks: Float,
    ) {
        state.variant = parrotVariant.select(avatarState).variant ?: getRandom(avatarState, parrotVariants)
    }

    enum class Variant(val variant: Parrot.Variant?) : Translatable {
        RANDOM(null),

        RED_BLUE(Parrot.Variant.RED_BLUE),
        BLUE(Parrot.Variant.BLUE),
        GREEN(Parrot.Variant.GREEN),
        YELLOW_BLUE(Parrot.Variant.YELLOW_BLUE),
        GRAY(Parrot.Variant.GRAY),
        ;

        override fun getTranslationKey(): String = createTranslationKey("parrot", "variant", name)
    }
}
