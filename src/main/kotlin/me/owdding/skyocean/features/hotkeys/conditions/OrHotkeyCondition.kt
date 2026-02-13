package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import earth.terrarium.olympus.client.ui.UIIcons
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.features.hotkeys.WidgetContext
import me.owdding.skyocean.features.item.custom.ui.standard.PADDING
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.extensions.bottomCenter
import me.owdding.skyocean.utils.extensions.createButton
import me.owdding.skyocean.utils.extensions.createText
import me.owdding.skyocean.utils.extensions.framed
import me.owdding.skyocean.utils.extensions.topLeft
import me.owdding.skyocean.utils.extensions.topRight
import me.owdding.skyocean.utils.extensions.withPadding
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.text.Text

@GenerateCodec
data class OrHotkeyCondition(
    val conditions: MutableList<HotkeyCondition> = mutableListOf(),
) : HotkeyCondition {
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.OrHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.OR

    override fun test(): Boolean = conditions.any { it.test() }

    context(context: WidgetContext)
    override fun asLayoutElement(selector: LayoutElement): LayoutElement = LayoutFactory.vertical(PADDING) {
        spacer(context.width)
        widget(selector, topLeft)

        createText("Children", CatppuccinColors.Mocha.surface0).withPadding(left = PADDING).add()
        LayoutFactory.vertical(PADDING) {
            val childContext = context.push()
            context(childContext) {
                conditions.forEach { hotkey ->
                    hotkey.toWidget(
                        {
                            LayoutFactory.frame(childContext.width) {
                                it.add(topLeft)
                                createButton(
                                    texture = null,
                                    icon = UIIcons.X,
                                    color = CatppuccinColors.Mocha.surface0Color,
                                    click = {
                                        conditions.remove(hotkey)
                                        context.rebuild()
                                    },
                                ).withPadding(PADDING, bottom = 0).add(topRight)
                            }
                        },
                    ) {
                        conditions[conditions.indexOf(hotkey)] = it
                        context.rebuild()
                    }.add(bottomCenter)
                    childContext.advance()
                }
            }
        }.add(bottomCenter)


        val text = Text.of("Add Condition", CatppuccinColors.Mocha.surface0)
        createButton(
            texture = null,
            icon = UIIcons.PLUS,
            color = CatppuccinColors.Mocha.surface0Color,
            text = text,
            hover = text,
            width = McFont.width(text) + 20,
            height = 20,
            leftClick = {
                conditions.add(AlwaysHotkeyCondition)
                context.rebuild()
            },
        ).withPadding(PADDING, top = 0).add(bottomCenter)
    }.framed(context.width)
}
