package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalColor
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.item.DyeColor

@RegisterAnimalModifier
object ShulkerModifier : AnimalModifier<Shulker, ShulkerRenderState> {
    override val type: EntityType<Shulker> = EntityType.SHULKER
    val colors = DyeColor.entries

    var shulkerColor = PlayerAnimalConfig.createEntry("shulker_color") { id, type ->
        enum(id, AnimalColor.RANDOM) {
            this.translation = createTranslationKey("shulker", "${type}_color")
            condition = isSelected(EntityType.SHULKER)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: ShulkerRenderState,
        partialTicks: Float,
    ) {
        state.color = shulkerColor.select(avatarState).dyeColor ?: getRandom(avatarState, colors)
    }
}
