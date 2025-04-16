package codes.cookies.skyocean.utils

import codes.cookies.skyocean.helpers.TooltipBuilder
import it.unimi.dsi.fastutil.objects.ObjectSortedSets
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic

fun ItemStack.withTooltip(init: TooltipBuilder.() -> Unit = {}): ItemStack {
    val builder = TooltipBuilder().apply(init).lines().filterIsInstance<Component>()
    when {
        builder.isEmpty() -> this.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(true, ObjectSortedSets.emptySet()))
        builder.size == 1 -> this.set(
            DataComponents.CUSTOM_NAME,
            builder.first().copy().apply {
                this.italic = false
            },
        )

        else -> {
            this.set(
                DataComponents.CUSTOM_NAME,
                builder.first().copy().apply {
                    this.italic = false
                },
            )
            val lore = builder.drop(1)
            this.set(DataComponents.LORE, ItemLore(lore, lore))
        }
    }
    return this
}
