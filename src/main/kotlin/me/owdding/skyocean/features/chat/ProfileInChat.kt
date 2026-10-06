package me.owdding.skyocean.features.chat

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MemoizeUtil
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.features.text.MarkdownChat.toComponent
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.Utils.set
import me.owdding.skyocean.utils.Utils.visitSiblings
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.util.StringDecomposer
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription.Companion.LOWEST
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabListChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketEvent
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.components.ComponentStateMachine
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import java.util.Optional
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Module
object ProfileInChat {
    private val hover = Text.of {
        append(ChatUtils.ICON_SPACE_COMPONENT)
        append("Added by SkyOcean!")
        this.color = TextColor.GRAY
    }

    private val profileTypeLookup = MemoizeUtil.memoize { icon: Char, color: Int ->
        Text.of("$icon ") {
            this.color = color
            this.hover = ProfileInChat.hover
        }
    }

    private val matcher = ComponentStateMachine.build {
        capture("before") {
            fork {
                literal("From ")
                literal("To ")
                literal("Guild > ")
                literal("Party > ")
                literal("Co-op > ")
                literal("Officer > ")
                // All chat
                branch {
                    optional {
                        char('[')
                        chars('0'..'9', maxLength = 4)
                        literal("] ")
                    }
                    optional {
                        wildcard({ it > 'z' }, 1) // emblem
                        literal(" ")
                    }
                }
            }
        }
        optional {
            literal(" ")
        }
        capture("after") {
            optional {
                char('[')
                wildcard({ it != ']' })
                literal("] ")
            }
            capture("name") {
                wildcard({ it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '_' }, 16)
            }
            wildcard(char = { true })
        }
    }

    private val profileTypes = mapOf(
        '♲' to TextColor.GRAY,
        'Ⓑ' to null,
        '☀' to TextColor.GREEN,
    )

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

            StringDecomposer.iterateFormatted(suffix, Style.EMPTY) { _, style, codepoint ->
                Character.toString(codepoint).forEach { char ->
                    if (profileTypes.containsKey(char)) {
                        val data = profileTypeLookup(char, style.color?.value ?: TextColor.WHITE)
                        usernameToProfileTypeCache[name] = data
                    }
                }
                true
            }
        }
    }

    @OnlyOnSkyBlock
    @Subscription(priority = LOWEST)
    fun onChat(event: ChatReceivedEvent.Post) {
        try {
            if (!ChatConfig.enableProfileInChat) return
            matcher.decompose(event.component) {
                val name = it["name"] ?: return@decompose
                val profileType = usernameToProfileTypeCache[name.toComponent().stripped] ?: return@decompose
                val before = it["before"] ?: return@decompose
                val after = it["after"] ?: return@decompose

                event.component = Text.of {
                    append(before.toComponent())
                    append(profileType)
                    append(after.toComponent())
                }
            }
        } catch (_: Exception) {
        }
    }

    @OnlyOnSkyBlock
    @Subscription(ServerChangeEvent::class)
    fun onServerChange() {
        usernameToProfileTypeCache.invalidateAll()
    }
}
