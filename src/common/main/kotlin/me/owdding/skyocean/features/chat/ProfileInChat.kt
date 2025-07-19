package me.owdding.skyocean.features.chat

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.chat.ChatConfig
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabListChangeEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object ProfileInChat {

    val usernameToProfileTypeMap = mutableMapOf<String, Component>()

    @Subscription
    @OnlyOnSkyBlock
    fun check(event: TabListChangeEvent) {
        McClient.players.forEach { player ->
            val name = player.profile.name
            val suffix = player.team?.playerSuffix ?: return@forEach
            suffix.siblings.forEach { sibling ->
                if (sibling.stripped in listOf("♲", "Ⓑ", "☀")) {
                    usernameToProfileTypeMap[name] = sibling
                }
            }
        }
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!ChatConfig.enableProfileInChat) return
        val index = event.component.siblings.indexOfFirst { sibling -> sibling.stripped.startsWith(": ") } - 1
        if (index < 0) return
        var name = event.component.siblings[index].stripped
        if (name.startsWith("] ")) {
            name = name.replace("] ", "")
        }
        val profileType = usernameToProfileTypeMap[name] ?: return
        event.cancel()
        val modified = event.component.copy()
        modified.siblings.add(index + 1, Text.of(" ").append(profileType))
        modified.send()
    }
}
