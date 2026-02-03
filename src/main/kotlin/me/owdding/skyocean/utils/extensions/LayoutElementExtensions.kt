package me.owdding.skyocean.utils.extensions

import com.teamresourceful.resourcefullib.common.color.Color
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.buttons.ButtonShape
import earth.terrarium.olympus.client.components.buttons.ButtonShapes
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.dropdown.DropdownBuilder
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.ColorableWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.OverlayAlignment
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.ui.context.ContextMenu
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.layouts.PaddedWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.accessors.ClearableLayout
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.components.CatppuccinColors
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.rendering.ExtraWidgetRenderers
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.layouts.*
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import org.jetbrains.annotations.Contract
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun Layout.tryClear() = apply {
    (this as? ClearableLayout)?.`skyocean$clear`()
}

fun <T : Layout> LayoutWidget<T>.clear() = apply { withContents { it.tryClear() } }

fun LayoutWidget<FrameLayout>.setFrameContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }
fun LayoutWidget<LinearLayout>.setLayoutContent(content: LayoutElement) = apply { clear().withContents { it.addChild(content) } }

fun LayoutElement.withPadding(
    padding: Int = 0,
    top: Int = padding,
    right: Int = padding,
    bottom: Int = padding,
    left: Int = padding,
): AbstractWidget = PaddedWidget(this, top, right, bottom, left)

fun Layout.asScrollableWidget(
    width: Int,
    height: Int,
    init: LayoutWidget<FrameLayout>.() -> Unit = {},
    alwaysShowScrollBar: Boolean = false,
): LayoutWidget<FrameLayout> {
    this.arrangeElements()
    val widget = LayoutWidget(this).apply {
        visible = true
        withAutoFocus(false)
    }.withStretchToContentSize()

    return widget.asScrollable(width, height, init, alwaysShowScrollBar)
}

fun AbstractWidget.asScrollable(
    width: Int,
    height: Int,
    init: LayoutWidget<FrameLayout>.() -> Unit = {},
    alwaysShowScrollBar: Boolean = false,
): LayoutWidget<FrameLayout> {
    val scrollable = Widgets.frame { frame ->
        frame.withScrollableY(TriState.of(alwaysShowScrollBar.takeIf { it }))
            .withSize(width, this.height.coerceAtMost(height))
            .withAutoFocus(false)
            .withContents { contents ->
                contents.setMinWidth(width - 10)
                contents.addChild(this, LayoutSettings.defaults().alignHorizontallyCenter())
            }
            .withAutoFocus(false)
            .init()
    }

    return scrollable
}

fun Layout.asScrollable(width: Int, height: Int, init: LayoutWidget<FrameLayout>.() -> Unit = {}, alwaysShowScrollBar: Boolean = false): Layout {
    this.arrangeElements()
    val widget = LayoutWidget(this).apply {
        visible = true
        withAutoFocus(false)
    }.withStretchToContentSize()

    return LayoutFactory.frame(width, height) {
        widget(widget.asScrollable(width, height, init, alwaysShowScrollBar))
    }
}

private val zeroDelay = (-1).seconds.toJavaDuration()

fun <T : AbstractWidget> T.withoutTooltipDelay(): T = apply {
    this.setTooltipDelay(zeroDelay)
}

fun createSprite(
    base: Identifier,
    disabledBase: Identifier = base,
    focused: Identifier = base,
    disabledFocused: Identifier = focused,
): WidgetSprites = WidgetSprites(base, disabledBase, focused, disabledFocused)

fun Identifier.asSprite() = WidgetSprites(this)

fun LayoutElement.framed(
    width: Int = this.width,
    height: Int = this.height,
    settings: LayoutSettings.() -> Unit = { middleCenter() },
): Layout = LayoutFactory.frame(width, height) {
    add(settings)
}

fun LayoutElement.withSolidBackground(color: Number, width: Int = this.width, height: Int = this.height): LayoutElement = LayoutFactory.frame {
    val element = this@withSolidBackground
    display(ExtraDisplays.solid(color, width, height))
    widget(element)
}

fun LayoutElement.withTexturedBackground(location: Identifier, width: Int = this.width, height: Int = this.height): LayoutElement = LayoutFactory.frame {
    val element = this@withTexturedBackground
    widget(Displays.sprite(location, width, height).asWidget()) {
        alignHorizontallyCenter()
        alignVerticallyMiddle()
    }
    widget(element) {
        alignHorizontallyCenter()
        alignVerticallyMiddle()
    }
}


fun LayoutElement.withTexturedBackground(location: String, width: Int = this.width, height: Int = this.height): LayoutElement = withTexturedBackground(
    SkyOcean.id(location), width, height,
)

fun Layout.asLayoutWidget(init: LayoutWidget<Layout>.() -> Unit = {}) = LayoutWidget(this).apply {
    visible = true
    withAutoFocus(false)
    init()
}

fun LayoutBuilder.string(text: String, color: Number = -1, settings: LayoutSettings.() -> Unit = {}) = widget(
    Widgets.text(
        Text.of(text) {
            this.color = color.toInt()
        },
    ),
    settings,
)

fun LayoutBuilder.string(text: Component, color: Number = -1, settings: LayoutSettings.() -> Unit = {}) = widget(
    Widgets.text(
        text.copy().apply {
            this.color = color.toInt()
        },
    ),
    settings,
)

fun Component.asWidget(): LayoutElement = Widgets.text(this)

fun <T> dropdown(
    state: DropdownState<T>,
    options: MutableList<T>,
    optionText: (T) -> Component,
    factory: Button.() -> Unit,
    builder: DropdownBuilder<T>.() -> Unit,
    optionFactory: (T) -> WidgetRenderer<Button>,
): Button {


    val button: Button = Widgets.button { btn ->
        btn.withRenderer(
            state.withRenderer { value, open ->
                (if (value == null) WidgetRenderers.ellpsisWithChevron(open) else WidgetRenderers.textWithChevron<Button>(
                    optionText(value),
                    open,
                )).withPadding(4, 6)
            },
        )
    }
    button.factory()

    val dropdown = button.withDropdown(state)
    dropdown.withOptions(options).withEntryRenderer(optionFactory)

    dropdown.builder()
    return dropdown.build()
}

fun LayoutSettings.middleCenter() {
    this.alignHorizontallyCenter()
    this.alignVerticallyMiddle()
}

fun LayoutSettings.middleLeft() {
    this.alignHorizontallyLeft()
    this.alignVerticallyMiddle()
}

fun LayoutSettings.middleRight() {
    this.alignHorizontallyRight()
    this.alignVerticallyMiddle()
}

fun LayoutSettings.bottomCenter() {
    this.alignHorizontallyCenter()
    this.alignVerticallyBottom()
}

fun LayoutSettings.bottomLeft() {
    this.alignHorizontallyLeft()
    this.alignVerticallyBottom()
}

fun LayoutSettings.bottomRight() {
    this.alignHorizontallyRight()
    this.alignVerticallyBottom()
}

fun LayoutSettings.topLeft() {
    this.alignHorizontallyLeft()
    this.alignVerticallyTop()
}

fun LayoutSettings.topRight() {
    this.alignHorizontallyRight()
    this.alignVerticallyTop()
}

fun LayoutSettings.topCenter() {
    this.alignHorizontallyCenter()
    this.alignVerticallyTop()
}

val middleCenter: LayoutSettings.() -> Unit get() = { middleCenter() }
val middleLeft: LayoutSettings.() -> Unit get() = { middleLeft() }
val middleRight: LayoutSettings.() -> Unit get() = { middleRight() }
val bottomCenter: LayoutSettings.() -> Unit get() = { bottomCenter() }
val bottomLeft: LayoutSettings.() -> Unit get() = { bottomLeft() }
val bottomRight: LayoutSettings.() -> Unit get() = { bottomRight() }
val topLeft: LayoutSettings.() -> Unit get() = { topLeft() }
val topRight: LayoutSettings.() -> Unit get() = { topRight() }
val topCenter: LayoutSettings.() -> Unit get() = { topCenter() }


fun createSpacer(width: Int = 0, height: Int = 0) = SpacerElement(width, height)
fun createSeparator(width: Int = 1, height: Int = 1) = createSpacer(width, height).withTexturedBackground("separator")

fun createTextInput(
    state: State<String>,
    texture: Identifier? = null,
    placeholder: String? = null,
    sprite: WidgetSprites? = texture?.asSprite(),
    textColor: Color? = null,
    placeholderColor: Color? = null,
    width: Int = 20,
    height: Int = 20,
): LayoutElement = Widgets.textInput(state).apply {
    withSize(width, height)
    placeholderColor?.let(::withPlaceholderColor)
    textColor?.let(::withTextColor)
    sprite?.let(::withTexture)
    placeholder?.let(::withPlaceholder)
}

fun createIntInput(
    state: State<Int>,
    texture: Identifier? = null,
    placeholder: String? = null,
    sprite: WidgetSprites? = texture?.asSprite(),
    textColor: Color? = null,
    placeholderColor: Color? = null,
    width: Int = 20,
    height: Int = 20,
): LayoutElement = Widgets.intInput(state).apply {
    withSize(width, height)
    placeholderColor?.let(::withPlaceholderColor)
    textColor?.let(::withTextColor)
    sprite?.let(::withTexture)
    placeholder?.let(::withPlaceholder)
}

fun createText(
    component: Component,
    width: Int? = null,
    init: MutableComponent.() -> Unit = {},
): TextWidget = Widgets.text(component.copy().apply(init)).apply {
    width?.let { this.width = it }
}

fun createText(
    string: String,
    color: Number = -1,
    width: Int? = null,
    init: MutableComponent.() -> Unit = {},
): TextWidget = Widgets.text(Text.of(string, color.toInt()).apply(init)).apply {
    width?.let { this.width = it }
}

fun createButton(builder: Button.() -> Unit): Button = Widgets.button().apply(builder)

@Contract("null,null->fail;_,null->!null")
private fun <Widget, Renderer> createRenderer(text: Component?, icon: Identifier?): Renderer
    where Widget : AbstractWidget, Renderer : WidgetRenderer<Widget>, Renderer : ColorableWidget, Renderer : Any = when {
    text != null && icon != null -> WidgetRenderers.textWithIcon<Widget>(text, icon).unsafeCast()
    text != null -> WidgetRenderers.text<Widget>(text).unsafeCast()
    icon != null -> WidgetRenderers.icon<Widget>(icon).unsafeCast()
    else -> throw UnsupportedOperationException("")
}

private fun <Renderer : ColorableWidget> Renderer.colored(color: Color) = apply { withColor(color) }

fun createToggleButton(
    state: State<Boolean>,
    trueText: String = "Enabled",
    falseText: String = "Disabled",
    trueColor: Number = CatppuccinColors.Mocha.green,
    falseColor: Number = CatppuccinColors.Mocha.red,
    texture: Identifier = id("hotkey/inset"),
    sprites: WidgetSprites? = createSprite(texture),
    trueComponent: Component = Text.of(trueText, trueColor.toInt()),
    falseComponent: Component = Text.of(falseText, falseColor.toInt()),
    componentProvider: (state: Boolean) -> Component = { if (it) trueComponent else falseComponent },
    width: Int = 20,
    height: Int = 20,
    onClick: () -> Unit = {},
) = createButton(
    text = componentProvider(state.get()),
    texture = sprites,
    width = width,
    height = height,
    click = {
        state.set(!state.get())
        onClick()
    },
)

fun createButton(
    texture: WidgetSprites? = UIConstants.BUTTON,
    shape: ButtonShape = ButtonShapes.RECTANGLE,
    icon: Identifier? = null,
    text: Component? = null,
    hoveredIcon: Identifier? = null,
    color: Color = MinecraftColors.WHITE,
    hoveredColor: Color = color,
    click: (() -> Unit)? = null,
    leftClick: (() -> Unit)? = null,
    rightClick: (() -> Unit)? = null,
    hover: Component? = null,
    width: Int = 12,
    height: Int = 12,
    builder: Button.() -> Unit = {},
): Button = Widgets.button().apply {
    withSize(width, height)
    click?.let { withCallback(click) }
    rightClick?.let { withCallback(GLFW.GLFW_MOUSE_BUTTON_RIGHT, rightClick) }
    leftClick?.let { withCallback(GLFW.GLFW_MOUSE_BUTTON_LEFT, leftClick) }
    if (icon != null || text != null) {
        withRenderer(
            ExtraWidgetRenderers.conditional(
                createRenderer<Button, _>(text, hoveredIcon ?: icon).colored(hoveredColor),
                createRenderer<Button, _>(text, icon).colored(color),
            ) {
                this.isHovered
            },
        )
    }
    hover?.let {
        withTooltip(hover)
        withoutTooltipDelay()
    }
    withShape(shape)
    withTexture(texture)
}.apply(builder)

fun setScreen(provider: () -> Screen?): () -> Unit = { McClient.setScreenAsync(provider) }

fun <T> createMultiselectDropdown(
    state: State<Set<T>>,
    options: List<T>,
    converter: (T) -> Component = { Text.of(it.toString()) },
    elementColor: Color = MinecraftColors.WHITE,
    buttonTexture: Identifier? = null,
    elementSprite: WidgetSprites = UIConstants.LIST_ENTRY,
    backgroundTexture: Identifier? = null,
    factory: Button.() -> Unit = {},
): Button {
    val state = DropdownState(null, state, false)
    state.button = Widgets.button {
        it.withRenderer(
            state.withRenderer { _, open ->
                WidgetRenderers.ellpsisWithChevron<Button>(open).withColor(CatppuccinColors.Mocha.surface0Color)
            }.withPadding(4, 6),
        )

        buttonTexture?.let { _ -> it.withTexture(createSprite(buttonTexture)) }
        it.apply(factory)
    }

    return state.button.withCallback {
        val entryWidth = state.button.width
        state.isOpened = true

        ContextMenu.open {
            it.withAutoCloseOff()
            it.withBounds(entryWidth, 150)
            it.withAlignment(OverlayAlignment.BOTTOM_LEFT, state)
            it.withCloseCallback { state.isOpened = false }
            backgroundTexture?.let(it::withTexture)

            for (option in options) {
                val text = converter(option)
                val textWidth = McFont.width(text)
                val textRenderer = WidgetRenderers.text<Button>(text).withColor(elementColor)
                val checkmark = WidgetRenderers.icon<Button>(UIIcons.CHECKMARK).withColor(elementColor)
                val selectedRenderer = WidgetRenderer { graphics, context, partialTick ->
                    val totalWidth = min(textWidth + 11 + PADDING, context.width)
                    val centerX = context.width / 2
                    val leftX = centerX - totalWidth / 2
                    checkmark.withCentered(11, 11).render(graphics, context.copy().setWidth(11).setX(context.x + leftX), partialTick)
                    textRenderer.render(graphics, context.copy().setX(context.x + leftX + 11 + PADDING).setWidth(textWidth), partialTick)
                }

                it.add {
                    Widgets.button()
                        .withTexture(elementSprite)
                        .withRenderer(
                            ExtraWidgetRenderers.conditional(
                                selectedRenderer,
                                textRenderer,
                            ) { state.get().contains(option) },
                        )
                        .withSize(entryWidth, 20)
                        .withCallback {
                            val selected = state.get().toMutableSet()
                            if (selected.contains(option)) selected.remove(option) else selected.add(option)
                            state.set(selected)
                        }
                }
            }
        }
    }
}
