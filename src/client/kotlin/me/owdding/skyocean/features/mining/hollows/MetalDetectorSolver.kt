package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ActionBarReceivedEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockAreas
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Module
object MetalDetectorSolver {

    val treasureRegex = Regex("""TREASURE: (\d+\.\d+)m""")

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onActionBar(event: ActionBarReceivedEvent.Pre) {
        if (LocationAPI.area == SkyBlockAreas.MINES_OF_DIVAN && McPlayer.heldItem.getData(DataTypes.ID)?.equals("DWARVEN_METAL_DETECTOR") == true) {
            val split = event.text.split("     ")
            for (widget in split) {
                if (widget.matches(treasureRegex)) {
                    treasureRegex.matchEntire(widget)?.let {
                        val distance = it.groupValues[1].toDouble()
                    }
                }
            }
        }
    }
}
