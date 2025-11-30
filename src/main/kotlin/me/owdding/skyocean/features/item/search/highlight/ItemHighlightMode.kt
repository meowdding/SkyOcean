package me.owdding.skyocean.features.item.search.highlight

import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName

enum class ItemHighlightMode {
    FILL,
    GLASS_PANE,
    ;

    private val displayName = toFormattedName()
    override fun toString(): String = displayName
}
