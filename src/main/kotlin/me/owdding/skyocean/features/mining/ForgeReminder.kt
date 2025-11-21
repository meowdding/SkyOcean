package me.owdding.skyocean.features.mining

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.helpers.CooldownHelper
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
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

    private val clickToCall by lazy {
        Text.of {
            append(" [") { color = TextColor.YELLOW }
            append("Click to call Fred") {
                color = TextColor.GOLD
                underlined = true
            }
            append("]") { color = TextColor.YELLOW }
            command = "/call fred"
            hover = Text.of("Call Fred!")
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

            val action = when (MiningConfig.forgeReminderAction) {
                ForgeReminderAction.WARP -> clickToWarp
                ForgeReminderAction.CALL_FRED -> clickToCall
                ForgeReminderAction.BOTH -> Text.join(clickToWarp, clickToCall)
            }

            Text.join(
                Text.translatable("skyocean.config.mining.forge_reminder"),
                ChatUtils.SEPERATOR_COMPONENT,
                items,
                action,
            ) {
                color = OceanColors.BASE_TEXT
            }.sendWithPrefix("SKYOCEAN_FORGE_REMINDER")
        },
    )

    @Subscription(ProfileChangeEvent::class)
    fun onProfileSwitch() {
        helper.reset()
    }

    enum class ForgeReminderAction : Translatable {
        WARP,
        CALL_FRED,
        BOTH,
        ;

        override fun getTranslationKey() = "skyocean.config.mining.forge_reminder_action.${name.lowercase()}"
    }

}
