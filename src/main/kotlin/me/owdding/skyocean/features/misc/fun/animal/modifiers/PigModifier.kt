package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.PigRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.animal.PigVariant
import net.minecraft.world.entity.animal.PigVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object PigModifier : AnimalModifier<Pig, PigRenderState> {
    override val type: EntityType<Pig> = EntityType.PIG

    val variants = Registries.PIG_VARIANT.list()

    var pigVariant = PlayerAnimalConfig.createEntry("pig_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("pig", "${type}_variant")
            condition = isSelected(EntityType.PIG)
        }
    }

    fun getPigVariant(state: AvatarRenderState): PigVariant = pigVariant.select(state).pigVariant ?: getRandom(state, variants)

    override fun apply(
        avatarState: AvatarRenderState,
        state: PigRenderState,
        partialTicks: Float,
    ) {
        state.variant = getPigVariant(avatarState)
    }

    enum class Variant(val resourceKey: ResourceKey<PigVariant>?) : Translatable {
        RANDOM(null),

        TEMPERATE(PigVariants.TEMPERATE),
        WARM(PigVariants.WARM),
        COLD(PigVariants.COLD),
        ;

        val pigVariant by lazy { resourceKey?.let { Registries.PIG_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("pig", "variant", name)
    }
}
