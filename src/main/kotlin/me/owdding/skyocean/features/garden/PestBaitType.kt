package me.owdding.skyocean.features.garden

import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
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
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import me.owdding.lib.displays.Alignment as DisplayAlignment

@Module
object PestBaitType : InventorySideGui("(?:Pest|Mouse|Vermin) Trap", { GardenConfig.pestBaitAlignment }) {

    override val enabled: Boolean get() = GardenConfig.pestBaitType && SkyBlockIsland.GARDEN.inIsland()

    override fun ContainerInitializedEvent.getLayout(): Layout = LayoutFactory.vertical(1) {
        horizontal {
            string(ChatUtils.ICON_SPACE_COMPONENT)
            string(-"garden.pest_bait_type")
        }

        Spray.entries.forEach { spray ->
            val pests = Pest.getPests(spray)
            val display = DisplayFactory.horizontal(alignment = DisplayAlignment.CENTER) {
                item(spray.itemStack)
                textDisplay(": ${pests.joinToString(", ") { it.displayName }}") {
                    color = TextColor.DARK_GRAY
                }
            }

            button {
                withTexture(null)
                withTooltip(Text.of("Click to get 64 ").append(spray.displayName).append(" from sacks!"))
                withSize(display.getWidth(), display.getHeight())
                withRenderer(DisplayWidget.displayRenderer(display))
                withCallback {
                    McClient.sendCommand("gfs ${spray.name} 64")
                }
            }
        }
    }
}
