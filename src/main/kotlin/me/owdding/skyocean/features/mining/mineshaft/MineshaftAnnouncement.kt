package me.owdding.skyocean.features.mining.mineshaft

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.CorpseType
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftType
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftType.*
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftEnteredEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftFoundEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object MineshaftAnnouncement {

    private var foundShaftType: Boolean = false
    private var foundCorpse: Boolean = false
    private var lastFound = 0L
    private var hasSend = false

    private var sentPartyMessage: String? = null

    private val shaftToColor = mapOf(
        // Gemstone
        TOPAZ to 0xde9000,
        SAPPHIRE to 0x4882ff,
        RUBY to 0xc5160d,
        PERIDOT to 0x7fb823,
        OPAL to 0xffffff,
        ONYX to 0x2f2f2f,
        JASPER to 0xe6387f,
        JADE to 0x229c57,
        CITRINE to 0xffba3a,
        AQUAMARINE to 0x00c6b6,
        AMETHYST to 0xc86eff,
        AMBER to 0xff7c00,
        // Other
        TITANIUM to 0xCECECF,
        TUNGSTEN to 0x484D53,
        UMBER to 0xD2752B,
        VANGUARD to 0x09D8EB,
    )

    private val corpseToColor = mapOf(
        CorpseType.VANGUARD to 0x09D8EB,
        CorpseType.TUNGSTEN to 0x484D53,
        CorpseType.UMBER to 0xD2752B,
        CorpseType.LAPIS to 0x345EC3,
    )

    @Subscription
    fun onShaftFind(event: MineshaftFoundEvent) {
        lastFound = System.currentTimeMillis()
    }

    @Subscription
    fun onShaftEnter(event: MineshaftEnteredEvent) {
        foundShaftType = true
        McClient.runNextTick { trySend() }
    }

    @Subscription
    fun onCorpseSpawn(event: CorpseSpawnEvent) {
        foundCorpse = true
        McClient.runNextTick { trySend() }
    }

    private fun trySend() {
        if (hasSend) return
        if (!foundShaftType || !foundCorpse) return
        if (!MineshaftConfig.shaftAnnouncement) return
        if (lastFound + 60000 < System.currentTimeMillis()) return

        val text = Text.join(
            "Mineshaft Entered",
            ChatUtils.SEPERATOR_COMPONENT,
            Text.of(MineshaftAPI.mineshaftType?.toFormattedName() ?: "Unknown Type", MineshaftAPI.mineshaftType.color()),
            " ",
            MineshaftAPI.mineshaftVariant?.toFormattedName() ?: "Unknown Variant",
            ChatUtils.SEPERATOR_COMPONENT,
            MineshaftAPI.corpses.groupBy { it.type }.toSortedMap(CorpseType::compareTo).map { (type, corpses) ->
                Text.join(
                    Text.of(corpses.size.toString()),
                    Text.of(type.name.first().toString(), type.color()),
                )
            }.let { Text.join(it, separator = Text.of(", ")) },
        ) {
            color = OceanColors.BASE_TEXT
        }

        if (MineshaftConfig.shaftAnnounceType == ShaftAnnounceType.PARTY) {
            if (PartyAPI.inParty) {
                Text.of("Sending message into party chat...", OceanColors.SEPARATOR).sendWithPrefix()
                McClient.sendCommand("/pc ${text.stripped}")
            }
        }
        ChatUtils.chat(text)
        sentPartyMessage = text.stripped
        hasSend = true
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (hasSend && sentPartyMessage?.let { event.text.endsWith(it) } == true) {
            sentPartyMessage = null
            event.cancel()
        }
    }

    @Subscription
    fun onWorldSwitch(event: ServerChangeEvent) = reset()

    private fun reset() {
        if (lastFound != 0L && !hasSend) {
            return
        }
        foundShaftType = false
        foundCorpse = false
        lastFound = 0L
        hasSend = false
        sentPartyMessage = null
    }

    private fun MineshaftType?.color() = shaftToColor[this] ?: TextColor.RED
    fun CorpseType?.color() = corpseToColor[this] ?: TextColor.RED

    enum class ShaftAnnounceType {
        CHAT,
        PARTY,
        ;

        val formatted = toFormattedName()
        override fun toString() = formatted
    }
}
