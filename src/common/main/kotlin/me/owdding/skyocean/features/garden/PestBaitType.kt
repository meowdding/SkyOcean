package me.owdding.skyocean.features.garden

import earth.terrarium.olympus.client.components.buttons.Button
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.config.features.garden.GardenConfig
import me.owdding.skyocean.helpers.InventorySideGui
import me.owdding.skyocean.utils.Utils.unaryMinus
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.api.area.farming.garden.pests.Pest
import tech.thatgravyboat.skyblockapi.api.area.farming.garden.pests.Spray
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object PestBaitType : InventorySideGui("(?:Pest|Mouse) Trap") {

    override val enabled: Boolean get() = GardenConfig.pestBaitType && SkyBlockIsland.GARDEN.inIsland()

    override fun ContainerInitializedEvent.getLayout(): Layout = LayoutFactory.vertical {
        horizontal {
            string(ChatUtils.ICON_SPACE_COMPONENT)
            string(-"garden.pest_bait_type")
        }

        Spray.entries.forEach { spray ->
            val pests = Pest.getPests(spray)

            horizontal(alignment = MIDDLE) {
                display(Displays.item(spray.itemStack))
                textDisplay(": ${pests.joinToString(", ") { it.displayName }}") {
                    color = TextColor.DARK_GRAY
                }
            }
        }

        val buttonDisplay = Displays.component(-"garden.open_sack").withPadding(2)
        val sacksButton = Button().apply {
            withRenderer(DisplayWidget.displayRenderer(buttonDisplay))
            setSize(buttonDisplay.getWidth(), buttonDisplay.getHeight())
            withCallback { McClient.sendCommand("/sacks") }
        }
        widget(sacksButton)
    }
}
