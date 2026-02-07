@file:Suppress("ConstPropertyName", "unused")

package me.owdding.skyocean.utils.chat

import com.teamresourceful.resourcefullib.common.color.Color

// Names and colors are from https://catppuccin.com/palette/
object CatppuccinColors {

    abstract class CatppuccinColorPalette {
        abstract val rosewater: Int
        val rosewaterColor: Color by lazy { Color(rosewater) }
        abstract val flamingo: Int
        val flamingoColor: Color by lazy { Color(flamingo) }
        abstract val pink: Int
        val pinkColor: Color by lazy { Color(pink) }
        abstract val mauve: Int
        val mauveColor: Color by lazy { Color(mauve) }
        abstract val red: Int
        val redColor: Color by lazy { Color(red) }
        abstract val maroon: Int
        val maroonColor: Color by lazy { Color(maroon) }
        abstract val peach: Int
        val peachColor: Color by lazy { Color(peach) }
        abstract val yellow: Int
        val yellowColor: Color by lazy { Color(yellow) }
        abstract val green: Int
        val greenColor: Color by lazy { Color(green) }
        abstract val teal: Int
        val tealColor: Color by lazy { Color(teal) }
        abstract val sky: Int
        val skyColor: Color by lazy { Color(sky) }
        abstract val sapphire: Int
        val sapphireColor: Color by lazy { Color(sapphire) }
        abstract val blue: Int
        val blueColor: Color by lazy { Color(blue) }
        abstract val lavender: Int
        val lavenderColor: Color by lazy { Color(lavender) }
        abstract val text: Int
        val textColor: Color by lazy { Color(text) }
        abstract val subtext1: Int
        val subtext1Color: Color by lazy { Color(subtext1) }
        abstract val subtext0: Int
        val subtext0Color: Color by lazy { Color(subtext0) }
        abstract val overlay2: Int
        val overlay2Color: Color by lazy { Color(overlay2) }
        abstract val overlay1: Int
        val overlay1Color: Color by lazy { Color(overlay1) }
        abstract val overlay0: Int
        val overlay0Color: Color by lazy { Color(overlay0) }
        abstract val surface2: Int
        val surface2Color: Color by lazy { Color(surface2) }
        abstract val surface1: Int
        val surface1Color: Color by lazy { Color(surface1) }
        abstract val surface0: Int
        val surface0Color: Color by lazy { Color(surface0) }
        abstract val base: Int
        val baseColor: Color by lazy { Color(base) }
        abstract val mantle: Int
        val mantleColor: Color by lazy { Color(mantle) }
        abstract val crust: Int
        val crustColor: Color by lazy { Color(crust) }
    }

    object Latte : CatppuccinColorPalette() {
        override val rosewater = 0xdc8a78
        override val flamingo = 0xdd7878
        override val pink = 0xea76cb
        override val mauve = 0x8839ef
        override val red = 0xd20f39
        override val maroon = 0xe64553
        override val peach = 0xfe640b
        override val yellow = 0xdf8e1d
        override val green = 0x40a02b
        override val teal = 0x179299
        override val sky = 0x04a5e5
        override val sapphire = 0x209fb5
        override val blue = 0x1e66f5
        override val lavender = 0x7287fd
        override val text = 0x4c4f69
        override val subtext1 = 0x5c5f77
        override val subtext0 = 0x6c6f85
        override val overlay2 = 0x7c7f93
        override val overlay1 = 0x8c8fa1
        override val overlay0 = 0x9ca0b0
        override val surface2 = 0xacb0be
        override val surface1 = 0xbcc0cc
        override val surface0 = 0xccd0da
        override val base = 0xeff1f5
        override val mantle = 0xe6e9ef
        override val crust = 0xdce0e8
    }


    object Frappe : CatppuccinColorPalette() {
        override val rosewater = 0xf2d5cf
        override val flamingo = 0xeebebe
        override val pink = 0xf4b8e4
        override val mauve = 0xca9ee6
        override val red = 0xe78284
        override val maroon = 0xea999c
        override val peach = 0xef9f76
        override val yellow = 0xe5c890
        override val green = 0xa6d189
        override val teal = 0x81c8be
        override val sky = 0x99d1db
        override val sapphire = 0x85c1dc
        override val blue = 0x8caaee
        override val lavender = 0xbabbf1
        override val text = 0xc6d0f5
        override val subtext1 = 0xb5bfe2
        override val subtext0 = 0xa5adce
        override val overlay2 = 0x949cbb
        override val overlay1 = 0x838ba7
        override val overlay0 = 0x737994
        override val surface2 = 0x626880
        override val surface1 = 0x51576d
        override val surface0 = 0x414559
        override val base = 0x303446
        override val mantle = 0x292c3c
        override val crust = 0x232634
    }

    object Macchiato : CatppuccinColorPalette() {
        override val rosewater = 0xf4dbd6
        override val flamingo = 0xf0c6c6
        override val pink = 0xf5bde6
        override val mauve = 0xc6a0f6
        override val red = 0xed8796
        override val maroon = 0xee99a0
        override val peach = 0xf5a97f
        override val yellow = 0xeed49f
        override val green = 0xa6da95
        override val teal = 0x8bd5ca
        override val sky = 0x91d7e3
        override val sapphire = 0x7dc4e4
        override val blue = 0x8aadf4
        override val lavender = 0xb7bdf8
        override val text = 0xcad3f5
        override val subtext1 = 0xb8c0e0
        override val subtext0 = 0xa5adcb
        override val overlay2 = 0x939ab7
        override val overlay1 = 0x8087a2
        override val overlay0 = 0x6e738d
        override val surface2 = 0x5b6078
        override val surface1 = 0x494d64
        override val surface0 = 0x363a4f
        override val base = 0x24273a
        override val mantle = 0x1e2030
        override val crust = 0x181926
    }

    object Mocha : CatppuccinColorPalette() {
        override val rosewater = 0xf5e0dc
        override val flamingo = 0xf2cdcd
        override val pink = 0xf5c2e7
        override val mauve = 0xcba6f7
        override val red = 0xf38ba8
        override val maroon = 0xeba0ac
        override val peach = 0xfab387
        override val yellow = 0xf9e2af
        override val green = 0xa6e3a1
        override val teal = 0x94e2d5
        override val sky = 0x89dceb
        override val sapphire = 0x74c7ec
        override val blue = 0x89b4fa
        override val lavender = 0xb4befe
        override val text = 0xcdd6f4
        override val subtext1 = 0xbac2de
        override val subtext0 = 0xa6adc8
        override val overlay2 = 0x9399b2
        override val overlay1 = 0x7f849c
        override val overlay0 = 0x6c7086
        override val surface2 = 0x585b70
        override val surface1 = 0x45475a
        override val surface0 = 0x313244
        override val base = 0x1e1e2e
        override val mantle = 0x181825
        override val crust = 0x11111b
    }
}
