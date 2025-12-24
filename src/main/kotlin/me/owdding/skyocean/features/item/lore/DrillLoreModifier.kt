package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.tags.SkyblockItemTagKey
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@ItemModifier
object DrillLoreModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.drill_modifications"
    override val isEnabled: Boolean get() = LoreModifierConfig.enableDrillCleanup

    override fun appliesTo(itemStack: ItemStack) = itemStack in SkyblockItemTagKey.DRILLS

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val fuelTank = item.getData(DataTypes.FUEL_TANK)
        val engine = item.getData(DataTypes.ENGINE)
        val upgradeModule = item.getData(DataTypes.UPGRADE_MODULE)

        fun MutableComponent.addOrNotInstalled(id: String?) {
            if (id != null) {
                append(RepoItemsAPI.getItemName(id))
            } else {
                append("Not Installed") { this.color = TextColor.RED }
            }
        }

        addUntil { it.stripped.contains("Tank") }

        add {
            append("Fuel Tank: ") { this.color = TextColor.GRAY }
            addOrNotInstalled(fuelTank)
        }
        skipUntilAfterSpace()

        add {
            append("Drill Engine: ") { this.color = TextColor.GRAY }
            addOrNotInstalled(engine)
        }
        skipUntilAfterSpace()

        add {
            append("Upgrade Module: ") { this.color = TextColor.GRAY }
            addOrNotInstalled(upgradeModule)
        }
        skipUntilAfterSpace()
        space()

        Result.modified
    }

}
