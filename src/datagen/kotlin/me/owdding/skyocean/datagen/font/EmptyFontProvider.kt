package me.owdding.skyocean.datagen.font

import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.chat.StylizedChatPrefixes
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput

private val emptyChars: List<Char> = buildList {
    addAll('a'..'z')
    addAll('A'..'Z')
    addAll('0'..'9')
    addAll("+<>-[](){},;.!?".toList())
    add(' ')
    add('\u200C')
}

// TODO: make this font be made via code, where all characters are empty with 0 width
class EmptyFontProvider(output: FabricPackOutput) : SkyOceanFontProvider(output, StylizedChatPrefixes.EMPTY_FONT) {

    override fun SkyOceanFontProviderHolder.create() {
        space {
            emptyChars.forEach { char ->
                add(char, 0)
            }
        }
    }

    override fun getName(): String = "Empty Font"
}
