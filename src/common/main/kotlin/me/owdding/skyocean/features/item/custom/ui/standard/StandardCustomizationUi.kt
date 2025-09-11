package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.modals.Modals
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.withPadding
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.layouts.asWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getOrCreateStaticData
import me.owdding.skyocean.features.item.custom.CustomItems.getOrTryCreateCustomData
import me.owdding.skyocean.features.item.custom.CustomItemsHelper
import me.owdding.skyocean.features.item.custom.data.ArmorTrim
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemSelectorOverlay
import me.owdding.skyocean.repo.customization.TrimPatternMap
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.ChatUtils.withoutShadow
import me.owdding.skyocean.utils.SkyOceanScreen
import me.owdding.skyocean.utils.Utils.asDisplay
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.wrapWithNotItalic
import me.owdding.skyocean.utils.asWidgetTable
import me.owdding.skyocean.utils.components.TagComponentSerialization
import me.owdding.skyocean.utils.items.ItemCache
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import me.owdding.skyocean.utils.rendering.StyledItemWidget
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.equipment.trim.TrimMaterial
import net.minecraft.world.item.equipment.trim.TrimPattern
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.compoundTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.jvm.optionals.getOrNull

object StandardCustomizationUi : SkyOceanScreen() {

    val buttons: MutableList<Button> = mutableListOf()
    var anyUpdated: Boolean = false
        set(value) {
            buttons.forEach { it.active = value }
            field = value
        }

    fun open(item: ItemStack) = ItemCustomizationModalBuilder().apply {
        anyUpdated = false
        buttons.clear()
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
            }.apply { buttons.add(this) },
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
            }.apply { buttons.add(this) },
        )
        buttons.forEach { it.active = false }
        withContent {
            LayoutFactory.horizontal(alignment = MIDDLE, spacing = 5) {
                vertical {
                    val name = CustomItemsHelper.getData(copiedItem, DataComponents.CUSTOM_NAME) ?: copiedItem.hoverName
                    val nameState = ListenableState.of(TagComponentSerialization.serialize(name))
                    nameState.registerListener { newName ->
                        anyUpdated = true
                        copiedItem.getOrCreateStaticData()?.let {
                            if (newName.isBlank()) {
                                it[CustomItemDataComponents.NAME] = null
                                return@let
                            }
                            it[CustomItemDataComponents.NAME] = TagComponentSerialization.deserialize(newName).wrapWithNotItalic()
                        }
                    }

                    horizontalDisplay {
                        this.display(Displays.text((!"Name ").withoutShadow()))
                        this.display(
                            Displays.sprite(SkyOcean.id("info"), 7, 7).withTooltip {
                                add("meow")
                            },
                        )
                    }
                    Widgets.textInput(nameState) {
                        it.withSize(160, 20)
                    }.add()
                    spacer(0, PADDING)

                    this.display(text("Model").withoutShadow().asDisplay())
                    Widgets.button {
                        it.withTexture(UIConstants.DARK_BUTTON)
                        it.withSize(160, 20)
                        fun update() {
                            val entry = CustomItems.staticMap[copiedItem.getKey()]?.get(CustomItemDataComponents.MODEL)?.toModelSearchEntry()
                            if (entry != null) {
                                it.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, entry, 20))
                            } else {
                                it.withRenderer(
                                    ItemSelectorOverlay.resolveRenderer(
                                        copiedItem,
                                        !BuiltInRegistries.ITEM.getKey(item.getItemModel()).path,
                                        20,
                                    ),
                                )
                            }
                        }

                        update()
                        it.withCallback {
                            update()
                            McClient.setScreen(ItemSelectorOverlay(McScreen.self, it, copiedItem))
                        }
                    }.add()

                    spacer(0, PADDING)
                    this.display(text("Trim").withoutShadow().asDisplay())
                    horizontal {
                        val trimData = CustomItemsHelper.getData(copiedItem, DataComponents.TRIM)
                        val trimPattern: ListenableState<TrimPattern?> = ListenableState.of(trimData?.pattern()?.value())
                        val trimMaterial: ListenableState<TrimMaterial?> = ListenableState.of(trimData?.material()?.value())

                        fun updateTrimData() {
                            // todo clear and highlight current selected :3
                            text {
                                append(trimPattern.get()?.assetId().toString())
                                append(" | ")
                                append(trimMaterial.get()?.description().toString())
                            }.sendWithPrefix()
                            val pattern = trimPattern.get() ?: return
                            val material = trimMaterial.get() ?: return
                            anyUpdated = true

                            copiedItem.getOrCreateStaticData()?.let {
                                it[CustomItemDataComponents.ARMOR_TRIM] = ArmorTrim(material, pattern)
                            }
                        }
                        trimPattern.registerListener { updateTrimData() }
                        trimMaterial.registerListener { updateTrimData() }
                        TrimPatternMap.map.map { (item, pattern) ->
                            Displays.item(item).withPadding(2).asButtonLeft {
                                trimPattern.set(pattern.takeUnless { trimPattern.get() == pattern })
                            }
                        }.chunked(5).asWidgetTable().asScrollableWidget(120, 60).add()
                        ItemCache.trimMaterials.map { item ->
                            Displays.item(item).withPadding(2).asButtonLeft {
                                val material =
                                    item.components().get(DataComponents.PROVIDES_TRIM_MATERIAL)?.material()?.unwrap(SkyOcean.registryLookup)?.getOrNull()
                                        ?.value() ?: run {
                                        (!"meow :(((((").sendWithPrefix()
                                        return@asButtonLeft
                                    }
                                trimMaterial.set(material.takeUnless { trimMaterial.get() == material })
                            }
                        }.chunked(2).asWidgetTable().asScrollableWidget(45, 60).add()
                    }
                }

                widget(StyledItemWidget(copiedItem).withSize(50, 65))
            }.asWidget()
        }

        withCloseCallback {
            if (showCloseWarning && anyUpdated) {
                Modals.action().apply {
                    withTitle(!"Save changes?")
                    withContent(
                        text {
                            append("Are you sure you want to exit? You currently have unsaved changes that would be lost!")
                            this.color = TextColor.WHITE
                        },
                    )
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
        buttons.clear()
        CustomItems.staticMap.remove(copy.getKey())
    }

    fun save(item: ItemStack, copy: ItemStack) {
        buttons.clear()
        val edited = CustomItems.staticMap[copy.getKey()] ?: return
        item.getOrTryCreateCustomData()?.data?.apply {
            clear()
            putAll(edited.data)
        }
        CustomItems.save()
    }

}
