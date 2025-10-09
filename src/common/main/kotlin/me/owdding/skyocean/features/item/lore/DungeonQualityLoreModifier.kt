package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

private const val MAX_DUNGEON_QUALITY = 50
private const val MAX_DUNGEON_TIER = 10

@ItemModifier
object DungeonQualityLoreModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.dungeon_quality"
    override val isEnabled: Boolean get() = LoreModifierConfig.dungeonQuality

    override fun appliesTo(itemStack: ItemStack) = itemStack.getData(DataTypes.DUNGEON_QUALITY) != null

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val quality = item.getData(DataTypes.DUNGEON_QUALITY) ?: 0
        val tier = item.getData(DataTypes.DUNGEON_TIER) ?: 0
        val qualityColor = if (quality >= MAX_DUNGEON_QUALITY) TextColor.RED else TextColor.PINK
        val tierColor = if (tier >= MAX_DUNGEON_TIER) TextColor.RED else TextColor.PINK

        copy()
        add(
            Text.of {
                this.color = TextColor.GRAY
                append("Item Quality: ")
                append(quality) { this.color = qualityColor }
                append("/")
                append(MAX_DUNGEON_QUALITY) { this.color = qualityColor }
            },
        )
        add(
            Text.of {
                this.color = TextColor.GRAY
                append("Tier: ")
                append(tier) { this.color = tierColor }
                append("/")
                append(MAX_DUNGEON_TIER) { this.color = tierColor }
            },
        )
        Result.modified
    }
}
