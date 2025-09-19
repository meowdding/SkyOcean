package me.owdding.skyocean.features.recipe.crafthelper.display

import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import tech.thatgravyboat.skyblockapi.helpers.McClient

enum class CraftHelperLocation(val position: (Int, Int) -> Pair<Int, Int>) {
    TOP_LEFT(
        { _, _ ->
            margin to margin
        },
    ),
    TOP_RIGHT(
        { width, _ ->
            screenWidth - width - margin to margin
        },
    ),
    BOTTOM_LEFT(
        { _, height ->
            margin to screenHeight - height - margin
        },
    ),
    BOTTOM_RIGHT(
        { width, height ->
            screenWidth - width - margin to screenHeight - height - margin
        },
    ),
    LEFT_OF_INVENTORY(
        { _, height ->
            margin to (screenHeight / 2 - height / 2)
        },
    ),
    RIGHT_OF_INVENTORY(
        { width, height ->
            screenWidth - width - margin to (screenHeight / 2 - height / 2)
        },
    ),
    ;

    companion object {
        private val screenWidth get() = McClient.window.guiScaledWidth
        private val screenHeight get() = McClient.window.guiScaledHeight
        private val margin get() = CraftHelperConfig.margin
    }
}
