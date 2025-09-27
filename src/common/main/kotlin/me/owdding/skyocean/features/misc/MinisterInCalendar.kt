package me.owdding.skyocean.features.misc

import com.mojang.authlib.properties.Property
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.hub.ElectionAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoMobsAPI
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.platform.ResolvableProfile
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitToWidth

@Module
object MinisterInCalendar {

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiscConfig.ministerInCalendar) return
        if (event.slot.index != 38) return
        if (event.title != "Calendar and Events") return
        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place minister item in calendar, item is not a glass pane")
            return
        }
        val minister = ElectionAPI.currentMinister ?: return
        val texture = RepoMobsAPI.getMobOrNull("${minister.name}_MAYOR")?.texture() ?: return

        event.item.skyoceanReplace {
            item = Items.PLAYER_HEAD

            set(
                DataComponents.PROFILE,
                ResolvableProfile {
                    put("textures", Property("textures", texture))
                },
            )

            name(
                Text.of("Minister ${minister.candidateName}") {
                    color = TextColor.ORANGE
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

                minister.activePerks.forEach { perk ->
                    add(perk.perkName) { color = TextColor.ORANGE }
                    perk.description.splitToWidth(" ", 140).forEach {
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
