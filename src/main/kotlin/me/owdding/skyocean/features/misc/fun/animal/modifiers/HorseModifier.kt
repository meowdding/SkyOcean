package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import earth.terrarium.olympus.client.utils.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.HorseRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.equine.Horse
import net.minecraft.world.entity.animal.equine.Variant
import net.minecraft.world.entity.animal.equine.Markings as HorseMarkings

@RegisterAnimalModifier
object HorseModifier : AnimalModifier<Horse, HorseRenderState> {
    override val type: EntityType<Horse> = EntityType.HORSE

    var horseColor = PlayerAnimalConfig.createEntry("horse_color") { id, type ->
        enum(id, Color.RANDOM) {
            this.translation = createTranslationKey("horse", "${type}_color")
            condition = isSelected(EntityType.HORSE)
        }
    }

    var horseMarking = PlayerAnimalConfig.createEntry("horse_marking") { id, type ->
        enum(id, Markings.RANDOM) {
            this.translation = createTranslationKey("horse", "${type}_marking")
            condition = isSelected(EntityType.HORSE)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: HorseRenderState,
        partialTicks: Float,
    ) {
        state.variant = horseColor.select(avatarState).variant ?: state.variant
        state.markings = horseMarking.select(avatarState).horseVariant ?: state.markings
    }

    enum class Color(val variant: Variant?) : Translatable {
        RANDOM(null),

        WHITE(Variant.WHITE),
        CREAMY(Variant.CREAMY),
        CHESTNUT(Variant.CHESTNUT),
        BROWN(Variant.BROWN),
        BLACK(Variant.BLACK),
        GRAY(Variant.GRAY),
        DARK_BROWN(Variant.DARK_BROWN),
        ;

        fun select(state: AvatarRenderState) = if (this == RANDOM) {
            getRandom(state, Variant.entries.filterNot { it == RANDOM.variant })
        } else {
            this
        }

        override fun getTranslationKey(): String = createTranslationKey("horse", "color", name)
    }

    enum class Markings(val horseVariant: HorseMarkings?) : Translatable {
        RANDOM(null),

        NONE(HorseMarkings.NONE),
        WHITE(HorseMarkings.WHITE),
        WHITE_FIELD(HorseMarkings.WHITE_FIELD),
        WHITE_DOTS(HorseMarkings.WHITE_DOTS),
        BLACK_DOTS(HorseMarkings.BLACK_DOTS)
        ;

        fun select(state: AvatarRenderState) = if (this == RANDOM) {
            getRandom(state, HorseMarkings.entries.filterNot { it == RANDOM.horseVariant })
        } else {
            this
        }

        override fun getTranslationKey(): String = createTranslationKey("horse", "marking", name)
    }
}
