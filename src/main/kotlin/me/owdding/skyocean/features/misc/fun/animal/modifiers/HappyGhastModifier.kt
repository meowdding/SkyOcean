package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.CollarColor
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.happyghast.HappyGhast
import net.minecraft.world.item.Items

@RegisterAnimalModifier
object HappyGhastModifier : AnimalModifier<HappyGhast, HappyGhastRenderState> {
    override val type: EntityType<HappyGhast> = EntityType.HAPPY_GHAST

    var harnessColor = PlayerAnimalConfig.createEntry("happy_ghast_harness") { id, type ->
        enum(id, CollarColor.DEFAULT) {
            this.translation = createTranslationKey("happy_ghast", "${type}_harness")
            condition = isSelected(EntityType.HAPPY_GHAST)
        }
    }

    val bodyItemMap = mapOf(
        CollarColor.WHITE to Items.WHITE_HARNESS,
        CollarColor.ORANGE to Items.ORANGE_HARNESS,
        CollarColor.MAGENTA to Items.MAGENTA_HARNESS,
        CollarColor.LIGHT_BLUE to Items.LIGHT_BLUE_HARNESS,
        CollarColor.YELLOW to Items.YELLOW_HARNESS,
        CollarColor.LIME to Items.LIME_HARNESS,
        CollarColor.PINK to Items.PINK_HARNESS,
        CollarColor.GRAY to Items.GRAY_HARNESS,
        CollarColor.LIGHT_GRAY to Items.LIGHT_GRAY_HARNESS,
        CollarColor.CYAN to Items.CYAN_HARNESS,
        CollarColor.PURPLE to Items.PURPLE_HARNESS,
        CollarColor.BLUE to Items.BLUE_HARNESS,
        CollarColor.BROWN to Items.BROWN_HARNESS,
        CollarColor.GREEN to Items.GREEN_HARNESS,
        CollarColor.RED to Items.RED_HARNESS,
        CollarColor.BLACK to Items.BLACK_HARNESS,
    ).mapValues { (_, item) -> item.defaultInstance }
    val values = bodyItemMap.values.toList()

    override fun apply(
        avatarState: AvatarRenderState,
        state: HappyGhastRenderState,
        partialTicks: Float,
    ) {
        val color = harnessColor.select(avatarState)
        if (color == CollarColor.NONE) return
        state.bodyItem = bodyItemMap[color] ?: getRandom(avatarState, values)
    }
}
