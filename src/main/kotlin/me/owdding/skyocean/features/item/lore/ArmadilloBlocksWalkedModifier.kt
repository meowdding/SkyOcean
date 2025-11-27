package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.extentions.getTag
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.jvm.optionals.getOrNull

@ItemModifier
object ArmadilloBlocksWalkedModifier : AbstractItemModifier() {
    override val displayName: Component = +"config.lore_modifiers.armadillo_blocks_walked"
    override val isEnabled: Boolean get() = LoreModifierConfig.prehistoryEggBlocksWalked

    override fun appliesTo(itemStack: ItemStack): Boolean = itemStack.getSkyBlockId()?.let { it.isItem && it.cleanId.equals("PREHISTORIC_EGG", true) } == true

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        addUntilAfter { it.stripped.isEmpty() }
        space()
        add {
            append("Blocks Walked: ")
            this.color = TextColor.GRAY
            val blocksWalked = item.getTag("blocks_walked")?.asInt()?.getOrNull() ?: 0
            append(blocksWalked.toFormattedString()) {
                this.color = item[DataTypes.RARITY]?.color ?: TextColor.WHITE
            }
        }
        space()

        Result.modified
    }
}
