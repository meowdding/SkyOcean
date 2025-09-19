package me.owdding.skyocean.features.dungeons.gambling

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.type.EnumArgumentType
import me.owdding.skyocean.api.SkyOceanItemId.Companion.getSkyOceanId
import me.owdding.skyocean.config.features.dungeons.DungeonsConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonChestType
import me.owdding.skyocean.utils.Utils.containerItems
import me.owdding.skyocean.utils.Utils.getArgument
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.events.base.CancellableSkyBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderScreenBackgroundEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.*
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.contains
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object DungeonGambling {

    private val regex = "(?<type>\\w+) Chest".toRegex()

    val allowedDungeonGamblingChests = listOf(DungeonChestType.OBSIDIAN, DungeonChestType.BEDROCK)

    private var rendering = false
    private var menu = -1

    private fun cancelIfRendering(event: CancellableSkyBlockEvent) {
        if (!rendering) return
        event.cancel()
    }

    @Subscription
    fun onScreenChange(event: InventoryChangeEvent) {
        if (!DungeonsConfig.gamblingScreenEnabled) return
        val floor = when (event.item) {
            in Items.BARRIER -> DungeonAPI.dungeonFloor ?: return
            in Items.ARROW -> if (DungeonsConfig.gamblingInCroesus) {
                event.item.getRawLore().firstOrNull { it.startsWith("To ") }?.let { line ->
                    CroesusImpl.croesusLoreToFloor[line.substring(3)]
                } ?: return
            } else return

            else -> return
        }
        val chest = event.screen as? ContainerScreen ?: return
        val items = chest.menu.slots.containerItems()

        // Remove these lines when using ChestDumps to test
        val id = chest.menu.containerId
        if (id == menu) return
        menu = id

        val stringType = regex.findGroup(event.screen.title.stripped, "type") ?: return
        val type = DungeonChestType.getByName(stringType) ?: return
        if (type !in allowedDungeonGamblingChests) return

        val winner = items.sortedByDescending { itemStack -> itemStack.getItemValue().price }.first { it.getSkyOceanId() != null }

        DungeonGamblingRenderer.init(floor, type, winner)
    }

    @Subscription
    fun onScreenRender(event: RenderScreenBackgroundEvent) {
        val menu = (event.screen as? ContainerScreen)?.menu
        rendering = menu?.containerId == this.menu && DungeonGamblingRenderer.render(event.graphics)
        if (rendering) event.cancel()
    }

    @Subscription(ContainerCloseEvent::class)
    fun onScreenClose() {
        DungeonGamblingRenderer.cancel()
        rendering = false
        menu = -1
    }

    @Subscription
    fun onScreenMouseClicked(event: ScreenMouseClickEvent) = cancelIfRendering(event)

    @Subscription
    fun onScreenMouseReleased(event: ScreenMouseReleasedEvent) = cancelIfRendering(event)

    @Subscription
    fun onScreenKeyReleased(event: ScreenKeyReleasedEvent) = cancelIfRendering(event)

    @Subscription
    fun onScreenKeyPressed(event: ScreenKeyPressedEvent) {
        cancelIfRendering(event)
        if (event.key == InputConstants.KEY_ESCAPE && rendering) {
            DungeonGamblingRenderer.cancel()
            rendering = false
            menu = -1
        }
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("gambling") {
            then("floor", EnumArgumentType(DungeonFloor::class)) {
                then("chest", EnumArgumentType(DungeonChestType::class)) {
                    callback {
                        val floor = getArgument<DungeonFloor>("floor")!!
                        val chest = getArgument<DungeonChestType>("chest")!!

                        McClient.setScreenAsync { DungeonGamblingFakeScreen(floor, chest) }
                    }
                }
            }
        }
    }
}
