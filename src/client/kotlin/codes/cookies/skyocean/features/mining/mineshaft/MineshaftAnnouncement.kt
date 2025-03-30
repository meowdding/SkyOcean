@file:Suppress("unused")

package codes.cookies.skyocean.features.mining.mineshaft

import codes.cookies.skyocean.config.features.mining.MineshaftConfig
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.ChatUtils
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.CorpseType
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftEnteredEvent
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.MineshaftFoundEvent
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object MineshaftAnnouncement {

    private var foundShaftType: Boolean = false
    private var foundCorpse: Boolean = false
    private var lastFound = 0L
    private var hasSend = false

    @Subscription
    fun onShaftFind(event: MineshaftFoundEvent) {
        lastFound = System.currentTimeMillis()
    }

    @Subscription
    fun onShaftEnter(event: MineshaftEnteredEvent) {
        foundShaftType = true
        trySend()
    }

    @Subscription
    fun onCorpseSpawn(event: CorpseSpawnEvent) {
        foundCorpse = true
        trySend()
    }

    private fun trySend() {
        if (hasSend) return
        if (!foundShaftType || !foundCorpse) return
        if (!MineshaftConfig.shaftAnnouncement) return
        if (lastFound + 60000 < System.currentTimeMillis()) return

        val text = Text.join(
            "Mineshaft Entered | ",
            MineshaftAPI.mineshaftType?.toFormattedName() ?: "Unknown",
            if (MineshaftAPI.isCrystal) " Crystal" else null,
            " | ",
            MineshaftAPI.corpses.groupBy { it.type }.toSortedMap(CorpseType::compareTo).map { (type, corpses) ->
                "${corpses.size}${type.name.first()}"
            }.joinToString(", ")
        )

        when (MineshaftConfig.shaftAnnounceType) {
            ShaftAnnounceType.CHAT -> ChatUtils.chat(text)
            ShaftAnnounceType.PARTY -> {
                if (PartyAPI.inParty) {
                    ChatUtils.chat("Sending message into party chat...")
                    ChatUtils.command("/pc ${text.stripped}")
                } else ChatUtils.chat(text)
            }
        }

        hasSend = true
    }

    @Subscription
    fun onWorldSwitch(event: ServerChangeEvent) = reset()

    private fun reset() {
        foundShaftType = false
        foundCorpse = false
        lastFound = 0L
        hasSend = false
    }

    enum class ShaftAnnounceType {
        CHAT,
        PARTY,
        ;

        val formatted = toFormattedName()
        override fun toString() = formatted
    }
}
