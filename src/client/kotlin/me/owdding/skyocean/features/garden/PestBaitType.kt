package me.owdding.skyocean.features.garden

import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withPadding
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.garden.GardenConfig
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.Utils.unaryMinus
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import tech.thatgravyboat.skyblockapi.api.area.farming.garden.pests.Pest
import tech.thatgravyboat.skyblockapi.api.area.farming.garden.pests.Spray
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.render.RenderScreenForegroundEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.extentions.top
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object PestBaitType {

    private val display by lazy {
        Displays.background(
            SkyOcean.id("blank"),
            DisplayFactory.vertical {
                horizontal {
                    string(ChatUtils.ICON_SPACE_COMPONENT)
                    string(-"garden.pest_bait_type")
                }

                Spray.entries.forEach { spray ->
                    val pests = Pest.getPests(spray)

                    horizontal(alignment = Alignment.CENTER) {
                        display(Displays.item(spray.itemStack))
                        textDisplay(": ${pests.joinToString(", ") { it.toFormattedName() }}") {
                            color = TextColor.DARK_GRAY
                        }
                    }
                }
            }.withPadding(5),
        )
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.GARDEN)
    fun onRender(event: RenderScreenForegroundEvent) {
        if (!GardenConfig.pestBaitType) return
        val container = event.screen as? AbstractContainerScreen<*> ?: return
        if (container.title.stripped !in listOf("Pest Trap", "Mouse Trap")) return
        display.render(event.graphics, container.right + 5, container.top)
    }

}
