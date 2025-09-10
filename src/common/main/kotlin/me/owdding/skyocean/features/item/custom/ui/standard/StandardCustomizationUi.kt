package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.UIConstants
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.floor
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getCustomData
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getOrTryCreateCustomData
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemSelectorOverlay
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.compoundTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel

object StandardCustomizationUi : SkyOceanScreen() {

    fun open(item: ItemStack) = ItemCustomizationModalBuilder().apply {
        val copiedItem = itemBuilder(item.item) {
            copyFrom(item)
            this.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(
                    compoundTag {
                        putBoolean("skyocean:customization_item", true)
                    },
                ),
            )
        }
        CustomItems.staticMap[copiedItem.getKey()!!] = item.getOrTryCreateCustomData()!!.let { it.copy(key = copiedItem.getKey()!!, data = HashMap(it.data)) }

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
                    Widgets.button {
                        it.withTexture(UIConstants.LIST_ENTRY)
                        it.withSize(160, 20)
                        val entry = copiedItem.getCustomData()?.get(CustomItemDataComponents.MODEL)?.toModelSearchEntry()
                        if (entry != null) {
                            it.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, entry, 20))
                        } else {
                            it.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, !BuiltInRegistries.ITEM.getKey(item.getItemModel()).path, 20))
                        }
                        it.withCallback {
                            McClient.setScreen(ItemSelectorOverlay(McScreen.self, it, copiedItem))
                        }
                    }.add()
                }
                val width = 50
                val height = width * 1.1f
                display(Displays.entity(McPlayer.self!!, width, height.toInt(), (width / 3f).floor()))
            }.asWidget()
        }
    }.open()

}
