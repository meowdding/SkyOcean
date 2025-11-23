package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CowRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Cow
import net.minecraft.world.entity.animal.CowVariant
import net.minecraft.world.entity.animal.CowVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object CowModifier : AnimalModifier<Cow, CowRenderState> {
    override val type: EntityType<Cow> = EntityType.COW

    val variants = Registries.COW_VARIANT.list()

    var cowVariant = PlayerAnimalConfig.createEntry("cow_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("cow", "${type}_variant")
            condition = isSelected(EntityType.COW)
        }
    }

    fun getCowVariant(state: AvatarRenderState): CowVariant = cowVariant.select(state).cowVariant ?: getRandom(state, variants)

    override fun apply(
        avatarState: AvatarRenderState,
        state: CowRenderState,
        partialTicks: Float,
    ) {
        state.variant = getCowVariant(avatarState)
    }

    enum class Variant(val resourceKey: ResourceKey<CowVariant>?) : Translatable {
        RANDOM(null),

        TEMPERATE(CowVariants.TEMPERATE),
        WARM(CowVariants.WARM),
        COLD(CowVariants.COLD),
        ;

        val cowVariant by lazy { resourceKey?.let { Registries.COW_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("cow", "variant", "name")
    }
}
