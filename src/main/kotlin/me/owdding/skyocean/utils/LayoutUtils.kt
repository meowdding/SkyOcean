package me.owdding.skyocean.utils

import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.lib.builder.LayoutFactory
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement

fun List<List<LayoutElement>>.asWidgetTable(spacing: Int = 0): Layout {
    return LayoutFactory.vertical(spacing) {
        this@asWidgetTable.map {
            LayoutFactory.horizontal(spacing) {
                it.forEach(::widget)
            }
        }.forEach(::widget)
    }
}

fun List<LayoutElement>.asRow(spacing: Int = 0): Layout {
    return LayoutFactory.horizontal(spacing) {
        this@asRow.forEach(::widget)
    }
}

fun List<LayoutElement>.asColumn(spacing: Int = 0): Layout {
    return LayoutFactory.vertical(spacing) {
        this@asColumn.forEach(::widget)
    }
}

fun <T : Layout> T.asLayoutWidget(): LayoutWidget<T> = LayoutWidget(this).withStretchToContentSize()

fun LayoutElement.setPosition(position: Pair<Int, Int>) = this.setPosition(position.first, position.second)
