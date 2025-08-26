package me.owdding.skyocean.features.dungeons.gambling

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.type.EnumArgumentType
import me.owdding.skyocean.api.SkyOceanItemId.Companion.getSkyOceanId
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonChestType
import me.owdding.skyocean.utils.Utils.getArgument
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.lang.ref.WeakReference

@Module
object DungeonGambling {

    private val regex = "(?<type>\\w+) Chest".toRegex()

    private val entries = mutableListOf<WeakReference<ContainerScreen>>()

    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        val chest = event.screen as? ContainerScreen ?: return
        val items = chest.menu.slots.map { it.item }
        entries.removeIf { it.get() == null }
        if (entries.any { it.get() == chest }) return

        val floor = DungeonAPI.dungeonFloor ?: return
        val stringType = regex.findGroup(event.screen.title.stripped, "type") ?: return
        val type = DungeonChestType.getByName(stringType) ?: return

        val winner = items.first { it.getSkyOceanId() != null }

        val gamblingScreen = DungeonGamblingScreen(floor, type, winner, event.screen)
        entries.add(WeakReference(chest))
        McClient.setScreen(gamblingScreen)
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("gambling") {
            then("floor", EnumArgumentType(DungeonFloor::class)) {
                then("chest", EnumArgumentType(DungeonChestType::class)) {
                    callback {
                        val floor = getArgument<DungeonFloor>("floor")!!
                        val chest = getArgument<DungeonChestType>("chest")!!

                        McClient.setScreenAsync { DungeonGamblingScreen(floor, chest) }
                    }
                }
            }
        }
    }
}
