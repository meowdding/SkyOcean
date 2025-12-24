package me.owdding.skyocean.features.recipe.crafthelper

import earth.terrarium.olympus.client.ui.context.ContextMenu
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.overlays.Position
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.config.hidden.OverlayPositions
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.raw.RawFormatter
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.rendering.OceanTextures
import me.owdding.skyocean.utils.rendering.Overlay
import me.owdding.skyocean.utils.rendering.SkyOceanOverlay
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.milliseconds

@Overlay
object CraftHelperOverlay : SkyOceanOverlay() {
    private val layoutCache = CachedValue(250.milliseconds) {
        val state = state ?: return@CachedValue null
        LayoutFactory.vertical {
            val builder = WidgetBuilder(true) {}
            context(state) {
                widget(
                    Displays.row(
                        Displays.text(
                            text {
                                append(state.required.toFormattedString())
                                append("x ")
                                this.color = TextColor.GRAY
                            },
                        ),
                        Displays.text(builder.name()),
                    ).asWidget(),
                ) { alignHorizontallyCenter() }
            }
            spacer(height = 2)
            RawFormatter.WITHOUT_PREFIX.create(state, builder, ::widget)
        }
    }
    private val layout by layoutCache

    override val name: Component = +"overlays.crafthelper"
    override val position: Position = OverlayPositions.craftHelper
    private val background get() = CraftHelperConfig.overlayBackground
    val padding get() = if (background) 3 else 0
    override val bounds: Pair<Int, Int> get() = (layout?.width?.plus(padding * 2) ?: 0) to (layout?.height?.plus(padding * 2) ?: 0)
    val state get() = CraftHelperManager.lastEvaluatedRoot.get()

    override val enabled: Boolean get() = CraftHelperConfig.enableOverlay && state != null && LocationAPI.isOnSkyBlock

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (padding != 0) {
            graphics.drawSprite(OceanTextures.overlayBackground, 0, 0, bounds.first, bounds.second)
        }

        graphics.translated(padding, padding) {
            layout?.visitWidgets { it.render(graphics, -1, -1, partialTicks) }
        }
    }

    override fun onRightClick() = ContextMenu.open {
        it.button(+"overlays.background.${if (background) "disable" else "enable"}") {
            CraftHelperConfig.overlayBackground = !CraftHelperConfig.overlayBackground
        }
        it.divider()
        it.dangerButton(Text.translatable("mlib.overlay.edit.reset")) {
            position.resetPosition()
        }
    }
}
