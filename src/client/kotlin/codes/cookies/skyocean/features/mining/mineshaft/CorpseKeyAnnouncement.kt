package codes.cookies.skyocean.features.mining.mineshaft

import codes.cookies.skyocean.config.features.mining.MineshaftConfig
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.ChatUtils
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.CorpseType
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.profile.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text

@Module
object CorpseKeyAnnouncement {

    @Subscription
    fun onCorpseSpawn(event: CorpseSpawnEvent) {
        if (!MineshaftConfig.keyAnnouncement) return

        val corpses = CorpseType.entries.associateWith { corpse ->
            val amount = event.corpses.filter { corpse == it.type }.size
            val sackAmount = SacksAPI.sackItems[corpse.key] ?: 0
            amount to sackAmount
        }.filter { it.value.first > 0 && it.key != CorpseType.LAPIS }

        val text = Text.join(
            "Corpse Keys | ",
            corpses.map { (type, pair) ->
                val (count, keys) = pair
                "$count${type.name.first()} ($keys in sacks)"
            }.joinToString(", ")
        )

        ChatUtils.chat(text)
    }
}
