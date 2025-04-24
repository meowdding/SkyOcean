package codes.cookies.skyocean.features.misc

import codes.cookies.skyocean.SkyOcean
import codes.cookies.skyocean.config.features.misc.MiscConfig
import codes.cookies.skyocean.utils.ChatUtils
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import me.owdding.ktmodules.Module
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.api.area.hub.ElectionAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.api.remote.RepoMobsAPI
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitToWidth
import java.util.*

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
        val texture = RepoMobsAPI.getMobOrNull("${minister.name}_MAYOR")?.texture ?: return

        event.item.replaceVisually {
            item = Items.PLAYER_HEAD

            set(
                DataComponents.PROFILE,
                ResolvableProfile(
                    GameProfile(UUID.randomUUID(), "a").apply {
                        this.properties.put("textures", Property("textures", texture))
                    },
                ),
            )

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
                    it.description.splitToWidth(" ", 140).forEach {
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
