package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreCleanupConfig
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

private const val MAX_DUNGEON_QUALITY = 50

@LoreModifier
object DungeonQualityLoreModifier : AbstractLoreModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.dungeon_quality"
    override val isEnabled: Boolean get() = LoreCleanupConfig.dungeonQuality

    override fun appliesTo(item: ItemStack) = item.getData(DataTypes.DUNGEON_QUALITY) != null

    override fun modify(item: ItemStack, list: MutableList<Component>): Boolean {
        val quality = item.getData(DataTypes.DUNGEON_QUALITY) ?: 0
        val isBestQuality = quality >= MAX_DUNGEON_QUALITY && LoreCleanupConfig.dungeonQualityHighlight

        list.add(
            1,
            Text.of {
                this.color = if (isBestQuality) TextColor.GOLD else TextColor.GRAY
                this.bold = isBestQuality
                append("Item Quality: ")
                append(quality) { this.color = TextColor.PINK }
                append("/")
                append(MAX_DUNGEON_QUALITY) { this.color = TextColor.PINK }
            },
        )
        return true
    }
}
