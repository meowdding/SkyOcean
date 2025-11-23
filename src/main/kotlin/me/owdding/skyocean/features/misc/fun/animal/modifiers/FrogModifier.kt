package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.FrogRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.frog.Frog
import net.minecraft.world.entity.animal.frog.FrogVariant
import net.minecraft.world.entity.animal.frog.FrogVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object FrogModifier : AnimalModifier<Frog, FrogRenderState> {
    override val type: EntityType<Frog> = EntityType.FROG
    val variants = Registries.FROG_VARIANT.list()

    var frogVariant = PlayerAnimalConfig.createEntry("frog_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("frog", "${type}_variant")
            condition = isSelected(EntityType.FROG)
        }
    }

    fun getFrogVariant(state: AvatarRenderState): FrogVariant = frogVariant.select(state).frogVariant ?: getRandom(state, variants)

    override fun apply(
        avatarState: AvatarRenderState,
        state: FrogRenderState,
        partialTicks: Float,
    ) {
        state.texture = getFrogVariant(avatarState).assetInfo.texturePath
    }

    enum class Variant(val resourceKey: ResourceKey<FrogVariant>?) : Translatable {
        RANDOM(null),

        TEMPERATE(FrogVariants.TEMPERATE),
        WARM(FrogVariants.WARM),
        COLD(FrogVariants.COLD),
        ;

        val frogVariant by lazy { resourceKey?.let { Registries.FROG_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("frog", "variant", name)
    }
}
