package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.modals.Modals
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.floor
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getOrTryCreateCustomData
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemSelectorOverlay
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
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

    var anyUpdated: Boolean = false

    fun open(item: ItemStack) = ItemCustomizationModalBuilder().apply {
        anyUpdated = false
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

        var showCloseWarning = true
        withAction(
            Widgets.button {
                it.withTexture(UIConstants.DANGER_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(ExtraWidgetRenderers.text(!"Cancel"))
                it.withCallback {
                    showCloseWarning = false
                    reset(copiedItem)
                    McScreen.self?.onClose()
                }
            },
        )
        withAction(
            Widgets.button {
                it.withTexture(UIConstants.PRIMARY_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(ExtraWidgetRenderers.text(!"Save"))
                it.withCallback {
                    showCloseWarning = false
                    McScreen.self?.onClose()
                    save(item, copiedItem)
                }
            },
        )
        withContent {
            LayoutFactory.horizontal(alignment = MIDDLE) {
                vertical {
                    Widgets.button {
                        it.withTexture(UIConstants.LIST_ENTRY)
                        it.withSize(160, 20)
                        fun update() {
                            val entry = CustomItems.staticMap[copiedItem.getKey()]?.get(CustomItemDataComponents.MODEL)?.toModelSearchEntry()
                            if (entry != null) {
                                it.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, entry, 20))
                            } else {
                                it.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, !BuiltInRegistries.ITEM.getKey(item.getItemModel()).path, 20))
                            }
                        }

                        update()
                        it.withCallback {
                            update()
                            McClient.setScreen(ItemSelectorOverlay(McScreen.self, it, copiedItem))
                        }
                    }.add()
                }
                val width = 50
                val height = width * 1.1f
                display(Displays.entity(McPlayer.self!!, width, height.toInt(), (width / 3f).floor()))
            }.asWidget()
        }

        withCloseCallback {
            if (showCloseWarning && anyUpdated) {
                Modals.action().apply {
                    withTitle(!"Save changes?")
                    withAction(
                        Widgets.button().apply {
                            withTexture(UIConstants.DANGER_BUTTON)
                            withSize(80, 24)
                            withRenderer(ExtraWidgetRenderers.text("No"))
                            withCallback {
                                @Suppress("AssignedValueIsNeverRead") // it's a lie
                                showCloseWarning = false
                                reset(copiedItem)
                                McClient.setScreen(null)
                            }
                        },
                    )
                    withAction(
                        Widgets.button().apply {
                            withTexture(UIConstants.PRIMARY_BUTTON)
                            withSize(80, 24)
                            withRenderer(ExtraWidgetRenderers.text("Yes"))
                            withCallback {
                                @Suppress("AssignedValueIsNeverRead") // it's a lie
                                showCloseWarning = false
                                save(item, copiedItem)
                                McClient.setScreen(null)
                            }
                        },
                    )

                }.open()
                return@withCloseCallback false
            }

            true
        }
    }.open()

    fun reset(copy: ItemStack) {
        CustomItems.staticMap.remove(copy.getKey())
    }

    fun save(item: ItemStack, copy: ItemStack) {
        val edited = CustomItems.staticMap[copy.getKey()] ?: return
        item.getOrTryCreateCustomData()?.data?.apply {
            clear()
            putAll(edited.data)
        }
        CustomItems.save()
    }

}
