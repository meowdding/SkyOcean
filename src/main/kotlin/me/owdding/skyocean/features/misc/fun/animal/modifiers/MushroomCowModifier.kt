package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.MushroomCow

@RegisterAnimalModifier
object MushroomCowModifier : AnimalModifier<MushroomCow, MushroomCowRenderState> {
    override val type: EntityType<MushroomCow> = EntityType.MOOSHROOM
    private val mushroomCowVariants = MushroomCow.Variant.entries

    var mushroomCowVariant = PlayerAnimalConfig.createEntry("mooshroom_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("mooshroom", "${type}_variant")
            condition = isSelected(EntityType.MOOSHROOM)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: MushroomCowRenderState,
        partialTicks: Float,
    ) {
        state.variant = mushroomCowVariant.select(avatarState).variant ?: getRandom(avatarState, mushroomCowVariants)
    }

    enum class Variant(val variant: MushroomCow.Variant?) : Translatable {
        RANDOM(null),

        RED(MushroomCow.Variant.RED),
        BROWN(MushroomCow.Variant.BROWN),
        ;

        override fun getTranslationKey(): String = createTranslationKey("mooshroom", "variant", name)
    }
}
