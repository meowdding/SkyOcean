package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.CollarColor
import me.owdding.skyocean.features.misc.`fun`.animal.EntityTypes
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.block.BlockModelResolver
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.happyghast.HappyGhast
import net.minecraft.world.item.Items

@RegisterAnimalModifier
object HappyGhastModifier : AnimalModifier<HappyGhast, HappyGhastRenderState> {
    override val type: EntityType<HappyGhast> = EntityTypes.HAPPY_GHAST

    var harnessColor = PlayerAnimalConfig.createEntry("happy_ghast_harness") { id, type ->
        enum(id, CollarColor.DEFAULT) {
            this.translation = createTranslationKey("happy_ghast", "${type}_harness")
            condition = isSelected(EntityTypes.HAPPY_GHAST)
        }
    }

    val bodyItemMap by lazy {
        mapOf(
            CollarColor.WHITE to Items.HARNESS.white(),
            CollarColor.ORANGE to Items.HARNESS.orange(),
            CollarColor.MAGENTA to Items.HARNESS.magenta(),
            CollarColor.LIGHT_BLUE to Items.HARNESS.lightBlue(),
            CollarColor.YELLOW to Items.HARNESS.yellow(),
            CollarColor.LIME to Items.HARNESS.lime(),
            CollarColor.PINK to Items.HARNESS.pink(),
            CollarColor.GRAY to Items.HARNESS.gray(),
            CollarColor.LIGHT_GRAY to Items.HARNESS.lightGray(),
            CollarColor.CYAN to Items.HARNESS.cyan(),
            CollarColor.PURPLE to Items.HARNESS.purple(),
            CollarColor.BLUE to Items.HARNESS.blue(),
            CollarColor.BROWN to Items.HARNESS.brown(),
            CollarColor.GREEN to Items.HARNESS.green(),
            CollarColor.RED to Items.HARNESS.red(),
            CollarColor.BLACK to Items.HARNESS.black(),
        ).mapValues { (_, item) -> item.defaultInstance }
    }
    val values by lazy { bodyItemMap.values.toList() }

    override fun apply(
        resolver: BlockModelResolver,
        avatarState: AvatarRenderState,
        state: HappyGhastRenderState,
        partialTicks: Float,
    ) {
        val color = harnessColor.select(avatarState)
        if (color == CollarColor.NONE) return
        state.bodyItem = bodyItemMap[color] ?: getRandom(avatarState, values)
    }
}
