package me.owdding.skyocean.datagen.font

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.PngHolder
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.chat.StylizedChatPrefixes
import me.owdding.skyocean.features.chat.KnownChatPrefix
import me.owdding.skyocean.utils.Utils
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import org.apache.commons.io.output.ByteArrayOutputStream
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

private const val backgroundHeight = 7

class StylizedChatPrefixesFontProvider(output: FabricPackOutput) : SkyOceanFontProvider(output, StylizedChatPrefixes.STYLIZED_CHAT_PREFIXES) {
    private val chatPrefixesTexturePath: String = id.path

    private val chatPrefixesProvider: PackOutput.PathProvider = output.createPathProvider(RESOURCE_PACK, "textures/$chatPrefixesTexturePath")
    private val pngHolder = PngHolder(chatPrefixesProvider)

    override fun SkyOceanFontProviderHolder.create() {
        val blank = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/chat_prefixes_blank.png"))
        val left = blank.getSubimage(0, 0, 2, backgroundHeight)
        val middle = blank.getSubimage(2, 0, 6, backgroundHeight)
        val right = blank.getSubimage(8, 0, 3, backgroundHeight)

        val fontMap = getSmallFont()

        KnownChatPrefix.entries.forEach {
            val name = it.displayName
            val spacedWidth = smallFontWidth + 1
            // 2 blank before text, 4 blank after text
            val totalWidth = 2 + (name.length * spacedWidth) + 4 - 1
            val image = BufferedImage(totalWidth, backgroundHeight, BufferedImage.TYPE_INT_ARGB)

            val graphics = image.createGraphics()

            // Draw background with background color
            val backgroundColor = Color(it.color)
            graphics.drawImageTinted(left, 0, 0, backgroundColor)

            repeat(name.length) { index ->
                graphics.drawImageTinted(middle, left.width + index * spacedWidth, 0, backgroundColor)
            }
            graphics.drawImageTinted(right, totalWidth - right.width, 0, backgroundColor)

            val shadowColor = Color(0, 0, 0, 50)
            name.forEachIndexed { index, character ->
                val glyph = fontMap[character] ?: error("couldnt find image for character $character")
                val x = 2 + index * spacedWidth

                graphics.drawImageTinted(glyph, x + 1, 1, shadowColor)

                graphics.drawImage(glyph, 2 + index * spacedWidth, 1, null)
            }

            graphics.dispose()

            val outputStream = ByteArrayOutputStream()
            val hashingOutputStream = HashingOutputStream(Hashing.sha256(), outputStream)
            ImageIO.write(image, "PNG", hashingOutputStream)
            pngHolder.submit(SkyOcean.id(it.name.lowercase()), outputStream, hashingOutputStream)

            val id = SkyOcean.id("${it.name.lowercase()}.png").withPrefix("$chatPrefixesTexturePath/")
            bitmap(id, backgroundHeight) {
                row(it.icon)
            }
        }
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        super.run(output).join()
        return CompletableFuture.allOf(pngHolder.save(output))
    }

    override fun getName() = "Stylized Chat Prefixes Font Generator"
}
