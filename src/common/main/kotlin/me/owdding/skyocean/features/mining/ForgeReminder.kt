package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.forge.ForgeAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.command
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Module
object ForgeReminder {

    private var lastReminder = Instant.DISTANT_PAST
    private val clickToWarp by lazy {
        Text.of {
            append(" [") { color = TextColor.YELLOW }
            append("Click to warp") {
                color = TextColor.GOLD
                underlined = true
            }
            append("]") { color = TextColor.YELLOW }
            command = "warp forge"
            hover = Text.of("Warp to the Forge")
        }
    }

    @Subscription(TickEvent::class)
    fun onTick() {
        if (!MiningConfig.forgeReminder) return
        if (lastReminder.since() <= MiningConfig.forgeReminderDelay.minutes) return

        val forgeSlots = ForgeAPI.getForgeSlots().values.filter { it.expiryTime <= currentInstant() }.takeUnless { it.isEmpty() } ?: return
        val items = forgeSlots.groupBy { it.id }.values.joinToComponent(", ") {
            Text.of {
                append("${it.size}x ") { color = TextColor.GRAY }
                append(RepoItemsAPI.getItemName(it.first().id))
            }
        }

        Text.join(Text.translatable("skyocean.config.mining.forge_reminder"), " | ", items, clickToWarp).sendWithPrefix()
        lastReminder = currentInstant()
    }

    private fun <T> Collection<T>.joinToComponent(separator: String, transform: (T) -> Component) = joinToComponent(Text.of(separator), transform)
    private fun <T> Collection<T>.joinToComponent(separator: Component, transform: (T) -> Component) = Text.join(this.map(transform), separator = separator.copy())

}
