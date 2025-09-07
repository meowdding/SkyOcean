package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.modals.Modals
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.floor
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

object StandardCustomizationUi : SkyOceanScreen() {

    fun open(item: ItemStack) = Modals.action().apply {
        withTitle(
            text("Editing ") {
                append(item.hoverName)
            },
        )
        withAction(
            Widgets.button {
                it.withTexture(UIConstants.DANGER_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(WidgetRenderers.text<Button>(!"Cancel").withColor(MinecraftColors.WHITE))
            },
        )
        withAction(
            Widgets.button {
                it.withTexture(UIConstants.PRIMARY_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(WidgetRenderers.text<Button>(!"Save").withColor(MinecraftColors.WHITE))
            },
        )
        withContent {
            LayoutFactory.horizontal(alignment = MIDDLE) {
                vertical {

                }
                val width = 200
                val height = width * 1.1f
                display(Displays.entity(McPlayer.self!!, width, height.toInt(), (width / 3f).floor()))
            }.asWidget()
        }
    }.open()

}
