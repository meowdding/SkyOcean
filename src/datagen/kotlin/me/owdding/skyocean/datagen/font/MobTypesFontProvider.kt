package me.owdding.skyocean.datagen.font

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.PngHolder
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.utils.Utils
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import org.apache.commons.io.output.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

private const val fontWidth = 5
private const val fontHeight = 5
private const val backgroundHeight = 7

class MobTypesFontProvider(output: FabricDataOutput, val converter: (KnownMobIcon) -> String, val fontType: ResourceLocation) :
    SkyOceanFontProvider(output, fontType) {
    val mobTypesTexturePath: String = fontType.path

    val mobTypesProvider: PackOutput.PathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/$mobTypesTexturePath")
    val pngHolder = PngHolder(mobTypesProvider)

    override fun SkyOceanFontProviderHolder.create() {
        space {
            add(' ', 4)
            add("\u200C", 0)
        }

        val blank = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/mob_types_blank.png"))
        val left = blank.getSubimage(0, 0, 2, backgroundHeight)
        val middle = blank.getSubimage(2, 0, 6, backgroundHeight)
        val right = blank.getSubimage(9, 0, 2, backgroundHeight)

        val font = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/small.png"))
        val fontMap: Map<Char, BufferedImage> = ('a'..'z').mapIndexed { index, character ->
            character.uppercaseChar() to font.getSubimage(index * fontWidth, 0, fontWidth, fontHeight)
        }.toMap()

        KnownMobIcon.entries.forEach {
            val name = converter(it)
            val spacedWidth = fontWidth + 1
            val totalWidth = 4 + name.length * spacedWidth - 1
            val image = BufferedImage(totalWidth, backgroundHeight, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()
            graphics.drawImage(left, 0, 0, null)
            name.forEachIndexed { index, character ->
                graphics.drawImage(middle, 2 + index * spacedWidth, 0, null)
                graphics.drawImage(fontMap[character]!!, 2 + index * spacedWidth, 1, null)
            }
            graphics.drawImage(right, totalWidth - 2, 0, null)

            graphics.dispose()
            val outputStream = ByteArrayOutputStream()
            val hashingOutputStream = HashingOutputStream(Hashing.sha256(), outputStream)
            ImageIO.write(image, "PNG", hashingOutputStream)
            pngHolder.submit(SkyOcean.id(it.name.lowercase()), outputStream, hashingOutputStream)

            val id = SkyOcean.id("${it.name.lowercase()}.png").withPrefix("$mobTypesTexturePath/")
            bitmap(id, backgroundHeight) {
                row(it.icon)
            }
        }
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        super.run(output).join()
        return CompletableFuture.allOf(pngHolder.save(output))
    }

    override fun getName() = "Mob Types Font Generator ('$fontType')"
}
