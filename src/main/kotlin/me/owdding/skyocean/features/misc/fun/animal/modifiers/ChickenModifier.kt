package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.ChickenRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.ChickenVariant
import net.minecraft.world.entity.animal.ChickenVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object ChickenModifier : AnimalModifier<Chicken, ChickenRenderState> {
    override val type: EntityType<Chicken> = EntityType.CHICKEN
    val variants = Registries.CHICKEN_VARIANT.list()

    var chickenVariant = PlayerAnimalConfig.createEntry("chicken_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("chicken", "${type}_variant")
            condition = isSelected(EntityType.CHICKEN)
        }
    }

    fun getChickenVariant(state: AvatarRenderState): ChickenVariant = chickenVariant.select(state).variant ?: getRandom(state, variants)

    override fun apply(
        avatarState: AvatarRenderState,
        state: ChickenRenderState,
        partialTicks: Float,
    ) {
        state.variant = getChickenVariant(avatarState)
    }

    enum class Variant(val resourceKey: ResourceKey<ChickenVariant>?) : Translatable {
        RANDOM(null),

        TEMPERATE(ChickenVariants.TEMPERATE),
        WARM(ChickenVariants.WARM),
        COLD(ChickenVariants.COLD),
        ;

        val variant by lazy { resourceKey?.let { Registries.CHICKEN_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("chicken", "variant", name)
    }
}
