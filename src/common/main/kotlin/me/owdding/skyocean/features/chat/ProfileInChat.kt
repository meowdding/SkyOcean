package me.owdding.skyocean.features.chat

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.Utils.set
import me.owdding.skyocean.utils.Utils.visitSiblings
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription.Companion.LOWEST
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabListChangeEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Module
object ProfileInChat {

    private val profileTypes = listOf("♲", "Ⓑ", "☀")
    private val usernameToProfileTypeCache: Cache<String, Component> = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(10.minutes.toJavaDuration())
        .expireAfterWrite(10.minutes.toJavaDuration())
        .build()

    @Subscription(TabListChangeEvent::class)
    @OnlyOnSkyBlock
    fun onTablistUpdate() {
        if (!ChatConfig.enableProfileInChat) return
        McClient.players.forEach { player ->
            val name = player.profile.name
            val suffix = player.team?.playerSuffix ?: return@forEach
            suffix.visitSiblings { sibling ->
                if (sibling.stripped.trim() in profileTypes) {
                    usernameToProfileTypeCache[name] = Text.of {
                        append(sibling)
                        append(CommonComponents.SPACE)
                        hover = Text.of {
                            append(ChatUtils.ICON_SPACE_COMPONENT)
                            append("Added by SkyOcean!")
                            this.color = TextColor.GRAY
                        }
                    }
                }
            }
        }
    }

    @OnlyOnSkyBlock
    @Subscription(priority = LOWEST)
    fun onChat(event: ChatReceivedEvent.Post) {
        if (!ChatConfig.enableProfileInChat) return
        val index = event.component.siblings.indexOfFirst { sibling -> sibling.stripped.startsWith(": ") } - 1
        if (index < 0) return
        val name = event.component.siblings[index].stripped.trim().substringAfterLast(" ")
        val targetIndex = if (name.equals(McPlayer.name, true)) {
            event.component.siblings.indexOfFirst { sibling -> sibling.style.hoverEvent == null }
        } else index
        val profileType = usernameToProfileTypeCache[name] ?: return
        val modified = event.component.copy()
        modified.siblings.add(targetIndex, profileType)
        event.component = modified
    }
}
