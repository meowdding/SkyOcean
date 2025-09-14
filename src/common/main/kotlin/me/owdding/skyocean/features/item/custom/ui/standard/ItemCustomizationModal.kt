package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UITexts
import earth.terrarium.olympus.client.ui.modals.Modals
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.displays.*
import me.owdding.lib.layouts.withPadding
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getOrCreateStaticData
import me.owdding.skyocean.features.item.custom.CustomItems.getOrTryCreateCustomData
import me.owdding.skyocean.features.item.custom.CustomItemsHelper
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.anyUpdated
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.asScrollableWidget
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.buttonClick
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.buttons
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.reset
import me.owdding.skyocean.features.item.custom.ui.standard.StandardCustomizationUi.save
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemSelectorOverlay
import me.owdding.skyocean.repo.customization.DyeData
import me.owdding.skyocean.repo.customization.TrimPatternMap
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.ChatUtils.withoutShadow
import me.owdding.skyocean.utils.Utils.asDisplay
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.wrapWithNotItalic
import me.owdding.skyocean.utils.animation.AnimationManager
import me.owdding.skyocean.utils.animation.AnimationManager.Companion.addImmediately
import me.owdding.skyocean.utils.animation.AnimationManager.Companion.onPercentage
import me.owdding.skyocean.utils.animation.DeferredLayout.Companion.onAnimationStart
import me.owdding.skyocean.utils.animation.DeferredLayoutFactory
import me.owdding.skyocean.utils.animation.EasingFunctions
import me.owdding.skyocean.utils.asColumn
import me.owdding.skyocean.utils.asWidgetTable
import me.owdding.skyocean.utils.components.TagComponentSerialization
import me.owdding.skyocean.utils.extensions.associateWithNotNull
import me.owdding.skyocean.utils.extensions.setFrameContent
import me.owdding.skyocean.utils.items.ItemCache
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import me.owdding.skyocean.utils.rendering.StyledItemWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.equipment.trim.TrimMaterial
import net.minecraft.world.item.equipment.trim.TrimPattern
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.utils.extentions.compoundTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

const val PADDING: Int = 5
const val BUTTON_GAP: Int = 5
const val CONTENT_GAP: Int = 5
const val HEADER_HEIGHT: Int = 11

class ItemCustomizationModal(val item: ItemStack, parent: Screen?) : Overlay(parent) {
    var animationManager: AnimationManager? = null
    private var layout: Layout? = null
    var showCloseWarning = true

    val copiedItem = itemBuilder(item.item) {
        copyFrom(this@ItemCustomizationModal.item)
        this.set(
            DataComponents.CUSTOM_DATA,
            CustomData.of(
                compoundTag {
                    putBoolean("skyocean:customization_item", true)
                },
            ),
        )
    }

    override fun rebuildWidgets() {
        animationManager = null
        super.rebuildWidgets()
    }

    init {
        CustomItems.staticMap[copiedItem.getKey()!!] = item.getOrTryCreateCustomData()!!.let { it.copy(key = copiedItem.getKey()!!, data = HashMap(it.data)) }
    }

    val customData = copiedItem.getOrCreateStaticData()
    val canBeEquipped = copiedItem.has(DataComponents.EQUIPPABLE)
    val name: Component = CustomItemsHelper.getData(copiedItem, DataComponents.CUSTOM_NAME) ?: copiedItem.hoverName
    val nameState: ListenableState<String> = ListenableState.of(TagComponentSerialization.serialize(name)).apply {
        registerListener { newName ->
            anyUpdated = true
            copiedItem.getOrCreateStaticData()?.let {
                if (newName.isBlank()) {
                    it[CustomItemDataComponents.NAME] = null
                    return@let
                }
                it[CustomItemDataComponents.NAME] = TagComponentSerialization.deserialize(newName).wrapWithNotItalic()
            }
        }
    }
    val trimData = CustomItemsHelper.getData(copiedItem, DataComponents.TRIM)
    val trimPattern: ListenableState<TrimPattern?> = ListenableState.of(trimData?.pattern()?.value())
    val trimMaterial: ListenableState<TrimMaterial?> = ListenableState.of(trimData?.material()?.value())
    val dye: ListenableState<ItemColor?> = ListenableState.of<ItemColor?>(State.of(null)).apply {
        registerListener { dye ->
            copiedItem.getOrCreateStaticData()?.let {
                it[CustomItemDataComponents.COLOR] = dye
            }
        }
    }
    val hasTrim = trimData != null
    val dyeTabState: State<DyeTab> = State.of(DyeTab.STATIC)
    override fun init() {
        super.init()
        anyUpdated = false
        buttons.clear()
        val dyeTabState = ListenableState(dyeTabState)
        val defaultLayout = DeferredLayoutFactory.horizontal(0.5f)
        val colorLayout = DeferredLayoutFactory.horizontal(0.5f)
        animationManager = AnimationManager(this, 0.25.seconds, defaultLayout, EasingFunctions.easeInOutQuad)

        val itemPreviewWidget = StyledItemWidget(copiedItem)
        val totalWidth = 258
        val name = DeferredLayoutFactory.vertical {
            add(
                DisplayWidget(
                    Displays.row(
                        Displays.text((!"Name ").withoutShadow()),
                        Displays.sprite(SkyOcean.id("info"), 7, 7).withTooltip {
                            add("meow")
                        },
                    ),
                ),
            )
            add(Widgets.textInput(nameState)) {
                withSize(totalWidth, 20)
            }
        }

        val modelSelection = Widgets.button {
            it.withTexture(UIConstants.DARK_BUTTON)
            it.withSize(totalWidth - (if (canBeEquipped) 25 else 0), 20)
        }

        fun updateModelSelection(withText: Boolean = true) {
            val entry = CustomItems.staticMap[copiedItem.getKey()]?.get(CustomItemDataComponents.MODEL)?.toModelSearchEntry()
            if (!withText) {
                modelSelection.withRenderer(ExtraWidgetRenderers.display(ItemSelectorOverlay.getItemDisplay(copiedItem, 20)))
                return
            }
            if (entry != null) {
                modelSelection.withRenderer(ItemSelectorOverlay.resolveRenderer(copiedItem, entry, 20))
            } else {
                modelSelection.withRenderer(
                    ItemSelectorOverlay.resolveRenderer(
                        copiedItem,
                        !BuiltInRegistries.ITEM.getKey(item.getItemModel()).path,
                        20,
                    ),
                )
            }
        }
        updateModelSelection()

        val dyeLabel = text("Dye").withoutShadow().asDisplay().asWidget()
        val dye = Widgets.button {
            it.withSize(20, 20)
            it.withRenderer(
                WidgetRenderers.layered(
                    WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                    DisplayWidget.displayRenderer(
                        ExtraDisplays.passthrough(20, 22) {
                            val color = customData?.get(CustomItemDataComponents.COLOR)?.getColor() ?: 0xFFFFFF
                            val actualColor = if (it.isHoveredOrFocused) ARGB.scaleRGB(color, 2 / 3f) else color
                            fill(2, 2, 18, 16, ARGB.opaque(actualColor))
                        },
                    ),
                ),
            )
        }

        val trimLabel = text("Trim").withoutShadow().asDisplay().asWidget()
        val dyeCategoryLabel = text("Dyes").withoutShadow().asDisplay().asWidget()
        val trimPatternWidget = TrimPatternMap.map.trimButton(trimPattern)
            .chunked(7)
            .asWidgetTable()

        val dyeCategories = listOf(
            dyeTabButton(dyeTabState, DyeTab.STATIC),
            dyeTabButton(dyeTabState, DyeTab.ANIMATED),
            dyeTabButton(dyeTabState, DyeTab.GRADIENT),
        ).asColumn()

        val trimPatternOrDyeWidget = LayoutWidget(FrameLayout())
            .withStretchToContentSize()
            .withTexture(UIConstants.MODAL_INSET)
        val trimMaterialOrRecentDyeWidget = LayoutWidget(FrameLayout())
            .withStretchToContentSize()
            .withTexture(UIConstants.MODAL_INSET)

        val dyeSelectionWidget = LayoutWidget(FrameLayout())
            .withStretchToContentSize()
            .withTexture(UIConstants.MODAL_INSET)

        val staticDyeSelection = DyeData.staticDyes.map { (key, _) -> SkyOceanItemId.item(key) to SkyBlockDye(key) }.toMap().toDyeList()
        val animatedDyeSelection = DyeData.animatedDyes.map { (key, _) -> SkyOceanItemId.item(key) to AnimatedSkyBlockDye(key) }.toMap().toDyeList()

        dyeTabState.registerListener {
            when (it) {
                DyeTab.STATIC -> dyeSelectionWidget.setFrameContent(staticDyeSelection)
                DyeTab.ANIMATED -> dyeSelectionWidget.setFrameContent(animatedDyeSelection)
                DyeTab.GRADIENT -> dyeSelectionWidget.setFrameContent(TODO())
            }
        }
        dyeSelectionWidget.setFrameContent(staticDyeSelection)

        val trimMaterialWidget = ItemCache.trimMaterials.associateWithNotNull {
            it.components()
                .get(DataComponents.PROVIDES_TRIM_MATERIAL)
                ?.material()
                ?.unwrap(SkyOcean.registryLookup)
                ?.getOrNull()
                ?.value()
        }.trimButton(trimMaterial)
            .chunked(4)
            .asWidgetTable()

        fun swapToDyes() {
            if (this.animationManager?.current != colorLayout) {
                this.animationManager?.next = colorLayout
            }
        }

        fun swapToTrims() {
            if (this.animationManager?.current != defaultLayout) {
                this.animationManager?.next = defaultLayout
            }
        }
        swapToTrims()
        dye.withCallback {
            buttonClick()
            if (animationManager?.current == defaultLayout) {
                swapToDyes()
                return@withCallback
            }
        }
        modelSelection.withCallback {
            buttonClick()
            if (animationManager?.current == colorLayout) {
                swapToTrims()
                return@withCallback
            }

            updateModelSelection()
            McClient.setScreen(ItemSelectorOverlay(McScreen.self, modelSelection, copiedItem))
        }

        defaultLayout.vertical {
            addDeferred(name)
            spacer(height = PADDING)
            horizontal {
                vertical {
                    add(text("Model").asDisplay().asWidget()) {
                        addImmediately()
                    }
                    add(modelSelection) {
                        withSize(totalWidth - (if (canBeEquipped) 25 else 0), 20)
                        onPercentage(0.1) {
                            updateModelSelection()
                        }
                    }
                }

                if (canBeEquipped) {
                    spacer(PADDING)
                    vertical {
                        add(dyeLabel)
                        add(dye) {
                            withSize(20, 20)
                        }
                    }
                }
            }
            if (canBeEquipped) {
                spacer(height = PADDING)
                add(trimLabel) {
                    addImmediately()
                }
                horizontal {
                    add(trimPatternOrDyeWidget) {
                        onAnimationStart {
                            trimPatternOrDyeWidget.setFrameContent(trimPatternWidget)
                        }
                    }
                    spacer(PADDING)
                    add(trimMaterialOrRecentDyeWidget) {
                        onAnimationStart {
                            trimMaterialOrRecentDyeWidget.setFrameContent(trimMaterialWidget)
                        }
                    }
                }
            }
        }
        defaultLayout.spacer(PADDING)
        defaultLayout.add(itemPreviewWidget) {
            withSize(50, 65)
        }

        colorLayout.vertical {
            addDeferred(name)
            spacer(height = PADDING)
            horizontal {
                vertical {
                    add(text("Mo..").asDisplay().asWidget()) { addImmediately() }
                    add(modelSelection) {
                        withSize(20, 20)
                        onPercentage(0.9) {
                            updateModelSelection(false)
                        }
                    }
                }

                spacer(PADDING)
                vertical {
                    add(dyeLabel)
                    add(dye) {
                        withSize(totalWidth - 25, 20)
                    }
                }
            }
            spacer(height = PADDING)
            add(dyeCategoryLabel) {
                addImmediately()
            }
            horizontal {
                add(trimPatternOrDyeWidget) {
                    onAnimationStart {
                        trimPatternOrDyeWidget.setFrameContent(dyeCategories)
                    }
                }
                spacer(3)
                add(dyeSelectionWidget)
                spacer(3)
                add(trimMaterialOrRecentDyeWidget) {
                    onAnimationStart {
                        trimMaterialOrRecentDyeWidget.setFrameContent(trimMaterialWidget)
                    }
                }
            }

        }
        colorLayout.spacer(PADDING)
        colorLayout.add(itemPreviewWidget) {
            withSize(50, 65)
        }


        val actions = listOf(
            Widgets.button {
                it.withTexture(UIConstants.DANGER_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(ExtraWidgetRenderers.text(!"Cancel"))
                it.withCallback {
                    showCloseWarning = false
                    buttonClick()
                    reset(copiedItem)
                    McScreen.self?.onClose()
                }
            }.apply { buttons.add(this) },

            Widgets.button {
                it.withTexture(UIConstants.PRIMARY_BUTTON)
                it.withSize(80, 24)
                it.withRenderer(ExtraWidgetRenderers.text(!"Save"))
                it.withCallback {
                    buttonClick()
                    showCloseWarning = false
                    McScreen.self?.onClose()
                    save(item, copiedItem)
                }
            }.apply { buttons.add(this) },
        )
        val actionsHeight: Int = actions.stream().mapToInt(AbstractWidget::getHeight).max().orElse(20)
        val actionsWidth: Int = max(
            actions.sumOf { it.width } + (actions.size - 1) * BUTTON_GAP,
            150,
        )

        defaultLayout.applyDefault(animationManager!!)
        val minContentHeight: Int = 100 - HEADER_HEIGHT - actionsHeight - PADDING * 4
        val contentHeight: Int = defaultLayout.getLayout().height + CONTENT_GAP
        val contentWidth: Int = defaultLayout.getLayout().width

        val modalWidth: Int = max(contentWidth, actionsWidth) + PADDING * 2

        val closeButton = Widgets.button()
            .withTexture(null)
            .withRenderer(WidgetRenderers.sprite<Button?>(UIConstants.MODAL_CLOSE))
            .withCallback {
                buttonClick()
                this.onClose()
            }
            .withTooltip(UITexts.BACK)
            .withSize(11, 11)

        val contentLayout = Layouts.column().withGap(CONTENT_GAP)
        contentLayout.withChild(
            Layouts.row()
                .withChild(SpacerElement.width(PADDING))
                .withChild(defaultLayout.getLayout())
                .withChild(SpacerElement.width(PADDING)),
        )

        if (contentHeight < minContentHeight) {
            contentLayout.withChild(SpacerElement.height(minContentHeight - contentHeight - CONTENT_GAP))
        }

        this.layout = Layouts.column()
            .withGap(PADDING)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, HEADER_HEIGHT + PADDING * 2)
                    .withTexture(UIConstants.MODAL_HEADER)
                    .withContents {
                        it.addChild(
                            Widgets.labelled(
                                this.font,
                                text("Editing ") {
                                    append(item.hoverName)
                                },
                                closeButton,
                            ).withEqualSpacing(Orientation.HORIZONTAL),
                        )
                    }
                    .withContentFill()
                    .withContentMargin(PADDING),
            )
            .withChildren(contentLayout)
            .withChild(
                Widgets.frame()
                    .withSize(modalWidth, actionsHeight + PADDING * 2)
                    .withTexture(UIConstants.MODAL_FOOTER)
                    .withContents {
                        val eachWidth = (modalWidth - PADDING * 2 - PADDING * (actions.size - 1)) / actions.size
                        actions.forEach { action ->
                            action.width = eachWidth
                            it.addChild(action)
                        }
                    }
                    .withEqualSpacing(Orientation.HORIZONTAL)
                    .withContentMargin(PADDING),
            )
            .build { widget: AbstractWidget -> this.addRenderableWidget(widget) }

        FrameLayout.centerInRectangle(this.layout!!, this.rectangle)
    }

    override fun render(p0: GuiGraphics, p1: Int, p2: Int, p3: Float) {
        animationManager?.update()
        super.render(p0, p1, p2, p3)
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val layout = layout ?: return
        super.renderBackground(graphics, mouseX, mouseY, partialTick)
        this.renderTransparentBackground(graphics)

        graphics.drawSprite(
            UIConstants.MODAL,
            layout.x - 1, layout.y - 1,
            layout.width + 2, layout.height + 2,
        )
    }

    override fun resize(mc: Minecraft, width: Int, height: Int) {
        this.width = width
        this.height = height
        this.repositionElements()
    }

    override fun onClose() {
        if (canClose()) {
            super.onClose()
        }
    }

    fun canClose(): Boolean {
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
                            buttonClick()
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
                            buttonClick()
                            showCloseWarning = false
                            save(item, copiedItem)
                            McClient.setScreen(null)
                        }
                    },
                )

            }.open()
            return false
        }

        return true
    }

    fun <V> Map<Item, V>.trimButton(state: State<V>): List<AbstractWidget> = this.map { (item, value) ->
        Widgets.button {
            val display = Displays.item(item).withPadding(2, bottom = 4)
            it.withTexture(null)
            it.withSize(display.getWidth(), display.getHeight())
            it.withRenderer(
                WidgetRenderers.layered(
                    ExtraWidgetRenderers.conditional(
                        WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                        WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                    ) { state.get() == value },
                    DisplayWidget.displayRenderer(display),
                ),
            )
            it.withCallback {
                buttonClick()
                state.set(value.takeUnless { state.get() == value })
                updateTrimData()
            }
        }.withPadding(1)
    }

    fun dyeTabButton(state: State<DyeTab>, dyeTab: DyeTab): AbstractWidget = Widgets.button {
        val display = if (dyeTab.id != null)
            Displays.item(dyeTab.id.toItem()).withPadding(2, bottom = 4)
        else
            ExtraDisplays.passthrough(20, 22) {
                fill(2, 2, 18, 16, ARGB.opaque(0xFFFFFF))
            }
        it.withTexture(null)
        it.withSize(display.getWidth(), display.getHeight())
        it.withRenderer(
            WidgetRenderers.layered(
                ExtraWidgetRenderers.conditional(
                    WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                    WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                ) { state.get() == dyeTab },
                DisplayWidget.displayRenderer(display),
            ),
        )
        it.withCallback {
            buttonClick()
            state.set(dyeTab)
        }
    }.withPadding(1)

    private fun Map<SkyOceanItemId, ItemColor>.toDyeList(): AbstractWidget = this.map { (id, color) ->
        Widgets.button {
            val display = Displays.item(id.toItem()).withPadding(2, bottom = 4)

            it.withTexture(null)
            it.withSize(display.getWidth(), display.getHeight())
            it.withRenderer(
                WidgetRenderers.layered(
                    ExtraWidgetRenderers.conditional(
                        WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                        WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                    ) { dye.get() == color },
                    DisplayWidget.displayRenderer(display),
                ),
            )
            it.withCallback {
                buttonClick()
                dye.set(color.takeUnless { dye.get() == color })
            }
        }.withPadding(1)
    }.chunked(6).asWidgetTable().asScrollableWidget(22 * 6 + 9, 24 * 3, alwaysShowScrollBar = true)

    fun updateTrimData() {
        if (StandardCustomizationUi.debug) {
            text {
                append(trimPattern.get()?.assetId().toString())
                append(" | ")
                append(trimMaterial.get()?.description().toString())
            }.sendWithPrefix()
        }
        val pattern = trimPattern.get()
        val material = trimMaterial.get()
        if (pattern == null || material == null) {
            if (hasTrim) {
                anyUpdated = true
            }
            copiedItem.getOrCreateStaticData()?.let {
                it[CustomItemDataComponents.ARMOR_TRIM] = null
            }
            return
        }
        anyUpdated = true

        copiedItem.getOrCreateStaticData()?.let {
            it[CustomItemDataComponents.ARMOR_TRIM] = ArmorTrim(material, pattern)
        }
    }
}

enum class DyeTab(val id: SkyOceanItemId?, val tooltip: Component) {
    STATIC(SkyOceanItemId.item("dye_aquamarine"), !"Static dye"),
    ANIMATED(SkyOceanItemId.item("dye_snowflake"), !"Animated dye"),
    GRADIENT(null, !"Custom colors"),
    ;
}
