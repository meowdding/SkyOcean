package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.getTag
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import kotlin.jvm.optionals.getOrNull

@LoreModifier
object ArmadilloBlocksWalkedModifier : AbstractLoreModifier() {
    override val displayName: Component = +"config.lore_modifiers.armadillo_blocks_walked"
    override val isEnabled: Boolean get() = LoreModifierConfig.prehistoryEggBlocksWalked

    override fun appliesTo(item: ItemStack): Boolean = item.getSkyBlockId()?.let { it.isItem && it.cleanId.equals("PREHISTORIC_EGG", true) } == true

    override fun modify(item: ItemStack, list: MutableList<Component>): Boolean = withMerger(list) {
        copyAll()
        add {
            append("Blocks Walked: ")
            val blocksWalked = item.getTag("blocks_walked")?.asInt()?.getOrNull() ?: 0
            append(blocksWalked.toFormattedString())
        }

        true
    }
}
