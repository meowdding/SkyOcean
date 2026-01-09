package me.owdding.skyocean.features.mining

import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.RemoteRepoDelegate
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.codecs.CodecHelpers
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.DWARVEN_MINES
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object FetchurHelper {

    private const val CORRECT_ITEM = "thanks thats probably what i needed"

    private val regex = "^\\[NPC] Fetchur: (?<message>.+)".toRegex()

    @GenerateCodec
    data class FetchurItem(
        val message: String,
        @Compact val items: List<SkyBlockId>,
        val amount: Int,
        val override: String?,
    ) {
        val itemName: String = override ?: run {
            require(items.size == 1) { "Fetchur items must have exactly one item if no override is provided" }
            items.single().toItem().cleanName
        }
    }

    private val fetchurItems = Utils.loadRepoData("mining/fetchur", CodecHelpers.list<FetchurItem>())
    private var fetchurItem: FetchurItem? = null

    @Subscription
    @OnlyIn(DWARVEN_MINES)
    fun onChatReceived(event: ChatReceivedEvent.Pre) {
        val message = regex.findGroup(event.text, "message") ?: return
        if (message.equals(CORRECT_ITEM, true)) {
            reset()
            return
        }
        if (!MiningConfig.fetchurHelper) return
        val item = fetchurItems.find { it.message.equals(message, true) } ?: return
        fetchurItem = item
        McClient.runNextTick {
            text {
                append("Fetchur wants: ") { color = OceanColors.BASE_TEXT }
                append("${item.amount}x ") { color = TextColor.BLUE }
                append(item.itemName) { color = OceanColors.HIGHLIGHT }
            }.sendWithPrefix()
            // TODO: do item tracker stuff
        }
    }

    private fun reset() {
        fetchurItem = null
    }

    @Subscription(ProfileChangeEvent::class)
    fun onProfileChange() = reset()
}
