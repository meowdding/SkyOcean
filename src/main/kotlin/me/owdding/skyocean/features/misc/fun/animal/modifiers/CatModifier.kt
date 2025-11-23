package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CatRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Cat
import net.minecraft.world.entity.animal.CatVariant
import net.minecraft.world.entity.animal.CatVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object CatModifier : AnimalModifier<Cat, CatRenderState> {
    override val type: EntityType<Cat> = EntityType.CAT

    private val catVariants: List<CatVariant> = Registries.CAT_VARIANT.list().sortedBy { it.assetInfo.id.toString() }

    var catVariant = PlayerAnimalConfig.createEntry("cat_variant") { id, type ->
        enum(id, Variant.DEFAULT) {
            this.translation = createTranslationKey("cat", "${type}_variant")
            condition = isSelected(EntityType.CAT)
        }
    }

    fun getCatVariant(state: AvatarRenderState): CatVariant = catVariant.select(state).catVariant ?: getRandom(state, catVariants)

    override fun apply(
        avatarState: AvatarRenderState,
        state: CatRenderState,
        partialTicks: Float,
    ) {
        state.texture = getCatVariant(avatarState).assetInfo().texturePath()
        state.collarColor = getCollarColor(avatarState)
        state.isSitting = state.isCrouching
    }

    enum class Variant(val resourceKey: ResourceKey<CatVariant>?) : Translatable {
        DEFAULT(null),

        TABBY(CatVariants.TABBY),
        BLACK(CatVariants.BLACK),
        RED(CatVariants.RED),
        SIAMESE(CatVariants.SIAMESE),
        BRITISH_SHORTHAIR(CatVariants.BRITISH_SHORTHAIR),
        CALICO(CatVariants.CALICO),
        PERSIAN(CatVariants.PERSIAN),
        RAGDOLL(CatVariants.RAGDOLL),
        WHITE(CatVariants.WHITE),
        JELLIE(CatVariants.JELLIE),
        ALL_BLACK(CatVariants.ALL_BLACK),
        ;

        val catVariant by lazy { resourceKey?.let { Registries.CAT_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("cat", "variant", name)
    }
}
