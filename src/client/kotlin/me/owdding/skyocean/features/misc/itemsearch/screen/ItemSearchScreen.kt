package me.owdding.skyocean.features.misc.itemsearch.screen

import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays.background
import me.owdding.skyocean.SkyOcean.olympus
import me.owdding.skyocean.utils.SkyOceanScreen

object ItemSearchScreen : SkyOceanScreen() {

    override fun init() {
        val width = (width / 3).coerceAtLeast(100) + 50
        val height = (height / 3).coerceAtLeast(100) + 50
        LayoutFactory.frame {
            display(background(olympus("buttons/normal"), width, height))

            LayoutFactory.vertical {
                spacer(height = 5)
                horizontal {
                    spacer(5)
                    textInput(
                        state = ListenableState.of(""),
                        placeholder = "Search...",
                        width = 100,
                        onChange = ::refreshSearch,
                    )
                }
            }.add {
                alignHorizontallyLeft()
                alignVerticallyTop()
            }
        }.center().applyLayout()
    }

    fun refreshSearch(search: String) {

    }
}
