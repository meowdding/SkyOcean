package me.owdding.skyocean.features.misc.`fun`.animal

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
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
import net.minecraft.world.item.DyeColor
import kotlin.jvm.optionals.getOrNull

object CatModifier : AnimalModifier<Cat, CatRenderState> {
    override val type: EntityType<Cat> = EntityType.CAT

    private val catVariants: List<CatVariant> = Registries.CAT_VARIANT.list().sortedBy { it.assetInfo.id.toString() }
    private val dyeColors = DyeColor.entries

    fun getCollarColor(state: AvatarRenderState): DyeColor? {
        val collarColor = PlayerAnimalConfig.collarColor.select(state)
        if (collarColor == CollarColor.NONE) return null
        return collarColor.dyeColor ?: getRandom(state, dyeColors)
    }

    fun getCatVariant(state: AvatarRenderState): CatVariant = PlayerAnimalConfig.catVariant.select(state).catVariant ?: getRandom(state, catVariants)

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
        override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_cats.variant.${name.lowercase()}"
    }

    enum class CollarColor(val dyeColor: DyeColor?) : Translatable {
        DEFAULT(null),
        NONE(null),

        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK),
        ;

        override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_cats.color.${name.lowercase()}"
    }
}
