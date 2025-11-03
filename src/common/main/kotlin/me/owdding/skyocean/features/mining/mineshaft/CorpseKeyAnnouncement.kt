package me.owdding.skyocean.features.mining.mineshaft

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.mining.mineshaft.MineshaftAnnouncement.color
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.OceanColors
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.Corpse
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.CorpseType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.location.mineshaft.CorpseSpawnEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CorpseKeyAnnouncement {

    @Subscription
    fun onCorpseSpawn(event: CorpseSpawnEvent) {
        if (!MineshaftConfig.keyAnnouncement) return

        sendKeys(event.corpses)
    }

    private fun sendKeys(corpses: List<Corpse>) {
        val keys = CorpseType.entries.associateWith { corpse ->
            val amount = corpses.count { corpse == it.type }
            val sackAmount = SacksAPI.sackItems[corpse.key] ?: 0
            val enderChestAmount = StorageAPI.enderchests.flatMap { it.items }.filter { it.getData(DataTypes.ID) == corpse.key }.sumOf { it.count }
            val storageAmount = StorageAPI.backpacks.flatMap { it.items }.filter { it.getData(DataTypes.ID) == corpse.key }.sumOf { it.count }
            amount to sackAmount + enderChestAmount + storageAmount
        }.filter { it.value.first > 0 && it.key != CorpseType.LAPIS }

        if (keys.isEmpty()) return

        val text = Text.join(
            "Corpse Keys",
            ChatUtils.SEPERATOR_COMPONENT,
            keys.map { (type, pair) ->
                val (count, keys) = pair
                Text.join(
                    count.toString(),
                    Text.of(type.name.first().toString(), type.color()),
                    " (",
                    Text.of(keys.toString(), OceanColors.HIGHLIGHT),
                    " available)",
                )
            }.let { Text.join(it, separator = Text.of(", ")) },
        ) {
            color = OceanColors.BASE_TEXT
        }

        ChatUtils.chat(text)
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("test corpsekey") {
            callback {
                val test = CorpseType.entries.map(::Corpse)
                sendKeys(test)
            }
        }
    }
}
