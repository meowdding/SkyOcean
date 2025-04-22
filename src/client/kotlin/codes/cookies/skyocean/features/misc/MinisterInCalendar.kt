package codes.cookies.skyocean.features.misc

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.config.features.misc.MiscConfig
import codes.cookies.skyocean.utils.ChatUtils
import codes.cookies.skyocean.utils.Utils.split
import me.owdding.ktmodules.Module
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.hub.ElectionAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough

@Module
object MinisterInCalendar {

    private val titleRegex = "Calendar and Events".toRegex()

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiscConfig.ministerInCalendar) return
        if (event.slot.index != 38) return
        if (!titleRegex.matches(event.title)) return
        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to replace mayor item in calendar, item is not a glass pane")
            return
        }
        val minister = ElectionAPI.currentMinister ?: return

        event.item.replaceVisually {
            item = Items.PLAYER_HEAD // TODO: actual player head

            name(
                Text.of {
                    append(ChatUtils.ICON_SPACE_COMPONENT)
                    append("Minister ${minister.candidateName}") { color = TextColor.ORANGE }
                },
            )

            tooltip {
                add("The minister has been elected for") { color = TextColor.GRAY }
                val year = ElectionAPI.rawData?.mayor?.election?.year?.plus(1) ?: "§cUnknown§7"
                add("year $year by the whole SkyBlock") { color = TextColor.GRAY }
                add("community.") { color = TextColor.GRAY }
                space()
                add("--------------------------") {
                    strikethrough = true
                    color = TextColor.DARK_GRAY
                }

                minister.activePerks.forEach {
                    add(it.perkName) { color = TextColor.ORANGE }
                    val a =
                        "All Garden Visitors reward §aFine Flour§7 and §c+10% Copper §7for accepting offers. Invites §a5 §7unique visitors to your §aGarden §7and makes higher rarity visitors §a20% §7more likely to show up."
                    // todo: actual description
                    a.split(" ", 140).forEach {
                        add(it) { color = TextColor.GRAY }
                    }
                }

                add("--------------------------") {
                    strikethrough = true
                    color = TextColor.DARK_GRAY
                }
                space()
                add("The listed perk is available to") { color = TextColor.GRAY }
                add("all players until the closing of") { color = TextColor.GRAY }
                add("the next election.") { color = TextColor.GRAY }
            }
        }
    }

}
