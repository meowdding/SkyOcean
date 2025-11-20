package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@ItemModifier
object CompactLevelBarsModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.compact_level_bars"
    override val isEnabled: Boolean get() = LoreModifierConfig.compactLevelBars
    private val list = setOf(
        "Ways to Level Up",
        "Skill Related Tasks",
    )

    override fun appliesTo(item: ItemStack): Boolean {
        return McScreen.self?.title?.stripped in list && item.cleanName.endsWith(" tasks", true)
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {

        while (hasNext { it.stripped.startsWith("▶") }) {
            addUntil { it.stripped.trim().startsWith("▶") }

            val task = readSafe()?.copy()
            if (!canRead() || peek().stripped.contains("▶")) {
                task?.let(::add)
                return@withMerger null
            }
            val xp = readSafe()?.stripped?.trim()?.split("/")
            val current = xp?.getOrNull(0)?.toIntValue()
            val required = xp?.getOrNull(1)?.substringBefore(" ")?.toIntValue()

            if (task == null || xp == null || current == null || required == null) {
                task?.let { add(it) }
                continue
            }

            add {
                this.color = TextColor.DARK_GRAY
                append(task)
                append(" (")
                append(current.toFormattedString()) { this.color = if (current == required) TextColor.GREEN else TextColor.RED }
                append("/")
                append(required.toFormattedString()) {
                    this.color = TextColor.GREEN
                    append(" XP") { this.color = TextColor.AQUA }
                }
                append(")")
            }

            skipUntilAfterSpace()
        }

        space()

        Result.modified
    }
}
