package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreCleanupConfig
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.tags.SkyblockItemTagKey
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@LoreModifier
object DrillLoreModifier : AbstractLoreModifier() {
    override val displayName: Component = +"skyocean.config.lore_cleanup.drill_modifications"
    override val isEnabled: Boolean get() = LoreCleanupConfig.enableDrillCleanup

    override fun appliesTo(item: ItemStack) = item in SkyblockItemTagKey.DRILLS

    override fun modify(item: ItemStack, list: MutableList<Component>) = withMerger(list) {
        val fuelTank = item.getData(DataTypes.FUEL_TANK)
        val engine = item.getData(DataTypes.ENGINE)
        val upgradeModule = item.getData(DataTypes.UPGRADE_MODULE)

        fun skipTillNextSpace() {
            while (index + 1 < original.size && peek().stripped.trim().isNotEmpty()) {
                read()
            }
            if (index + 1 < original.size) read()
        }

        addUntil { it.stripped.contains("Tank") }
        if (fuelTank != null) {
            add {
                append("Fuel Tank: ") { this.color = TextColor.GRAY }
                append(RepoItemsAPI.getItemName(fuelTank))
            }
        } else {
            add {
                append("Fuel Tank: ") { this.color = TextColor.GRAY }
                append("Not Installed") { this.color = TextColor.RED }
            }
        }
        skipTillNextSpace()

        if (engine != null) {
            add {
                append("Drill Engine: ") { this.color = TextColor.GRAY }
                append(RepoItemsAPI.getItemName(engine))
            }
        } else {
            add {
                append("Drill Engine: ") { this.color = TextColor.GRAY }
                append("Not Installed") { this.color = TextColor.RED }
            }
        }
        skipTillNextSpace()

        if (upgradeModule != null) {
            add {
                append("Upgrade Module: ") { this.color = TextColor.GRAY }
                append(RepoItemsAPI.getItemName(upgradeModule))
            }
        } else {
            add {
                append("Upgrade Module: ") { this.color = TextColor.GRAY }
                append("Not Installed") { this.color = TextColor.RED }
            }
        }
        skipTillNextSpace()
        space()

        true
    }

}
