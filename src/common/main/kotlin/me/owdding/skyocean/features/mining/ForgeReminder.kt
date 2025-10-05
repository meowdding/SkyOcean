package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.helpers.CooldownHelper
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.extensions.joinToComponent
import me.owdding.skyocean.utils.extensions.nullIfEmpty
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
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
import kotlin.time.Duration.Companion.minutes

@Module
object ForgeReminder {

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

    private val helper = CooldownHelper(
        cooldown = { MiningConfig.forgeReminderDelay.minutes },
        onReady = {
            if (!MiningConfig.forgeReminder) return@CooldownHelper
            val now = currentInstant()
            val forgeSlots = ForgeAPI.getForgeSlots().values.filter { it.expiryTime <= now }.nullIfEmpty() ?: return@CooldownHelper
            val items = forgeSlots.groupBy { it.id }.values.joinToComponent(", ") {
                Text.of {
                    append("${it.size}x ") { color = TextColor.GRAY }
                    append(RepoItemsAPI.getItemName(it.first().id))
                }
            }

            Text.join(Text.translatable("skyocean.config.mining.forge_reminder"), " | ", items, clickToWarp).sendWithPrefix()
        },
    )

    @Subscription(ProfileChangeEvent::class)
    fun onProfileSwitch() {
        helper.reset()
    }

}
