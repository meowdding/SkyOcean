package me.owdding.skyocean.features.gambling.vanguard

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.config.features.gambling.GamblingConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.gambling.SlotMachineSpinner
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import net.minecraft.network.chat.ComponentSerialization
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@LateInitModule
object VanguardGambling {

    private val data = Utils.loadRepoData<Thing>("vanguard") { SkyOceanCodecs.getCodec<Thing>() }

    // TODO: ENCHANTED BOOK SUPPORT MAYBE
    private val startRegex = " {2}VANGUARD CORPSE LOOT! ?".toRegex()
    private val itemRegex = " {4}(?<item>.+?)(?: x(?<amount>[\\d,]+)|$)".toRegex()
    private val endRegex = "▬{64}".toRegex()

    private var parsing = false
    private val loot = mutableListOf<Pair<SkyBlockId, Int>>()

    private fun gamblingTime() {
        val sortedLoot = loot.sortedByDescending { it.first.toItem().getItemValue().price * it.second }

        val (id, _) = sortedLoot.find { it.first in data.valuables } ?: (null to 0)
        McClient.setScreenAsync { SlotMachineSpinner(data.items, id) }
    }

    @Subscription
    fun onMessage(event: ChatReceivedEvent.Pre) {
        if (!GamblingConfig.vanguardGambling) return
        when {
            startRegex.match(event.text) -> {
                parsing = true
                loot.clear()
            }

            endRegex.match(event.text) && parsing -> {
                parsing = false
                gamblingTime()
                loot.clear()
            }

            parsing -> {
                itemRegex.match(event.text) { match ->
                    val id = SkyBlockId.fromName(match["item"] ?: return@match, dropLast = false) ?: return@match
                    val count = match["amount"].toIntValue().coerceAtLeast(1)
                    loot.add(id to count)
                }
            }
        }
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDevWithCallback("gambling vanguard") {
            val possibleLoot = List(7) {
                val item = data.items.keys.random().toItem()
                item.cleanName to (item[DataTypes.RARITY]?.color ?: 0)
            }
            val components = listOf(
                "{\"text\":\"\",\"extra\":[{\"text\":\"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\",\"bold\":true,\"color\":\"green\"}],\"italic\":false}",
                "{\"text\":\"\",\"extra\":[\"  \",{\"text\":\"\",\"bold\":true,\"color\":\"aqua\"},{\"text\":\"VANGUARD \",\"bold\":true,\"color\":\"white\"},{\"text\":\"CORPSE LOOT! \",\"bold\":true,\"color\":\"aqua\"}],\"italic\":false}",
                "{\"text\":\"\",\"italic\":false}",
                "{\"text\":\"\",\"extra\":[\"  \",{\"text\":\"REWARDS\",\"bold\":true,\"color\":\"green\"}],\"italic\":false}",
                *possibleLoot.map {
                    "{\"text\":\"\",\"extra\":[\"    \",{\"text\":\"${it.first}\",\"color\":\"#${it.second.toHexString()}\"}],\"italic\":false}"
                }.toTypedArray(),
                "{\"text\":\"\",\"extra\":[{\"text\":\"▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\",\"bold\":true,\"color\":\"green\"}],\"italic\":false}",
            ).map { ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(it)).getOrThrow().first }

            components.forEach { component ->
                ChatReceivedEvent.Pre(component).post(SkyBlockAPI.eventBus)
                component.send()
            }
        }
    }

    @GenerateCodec
    data class Thing(
        val items: Map<SkyBlockId, Int>,
        val valuables: List<SkyBlockId>,
    )

}
