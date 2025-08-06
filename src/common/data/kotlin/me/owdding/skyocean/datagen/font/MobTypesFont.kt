package me.owdding.skyocean.datagen.font

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.providers.PngHolder
import me.owdding.skyocean.datagen.providers.SkyOceanFontProvider
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import me.owdding.skyocean.utils.Utils
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.PackOutput
import org.apache.commons.io.output.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

const val mobTypesTexturePath = "font/mob_types"

class MobTypesFont(output: FabricDataOutput) : SkyOceanFontProvider(output, MobIcons.FONT_ID) {
    val mobTypesProvider: PackOutput.PathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/$mobTypesTexturePath")
    val pngHolder = PngHolder(mobTypesProvider)

    override fun SkyOceanFontProviderHolder.create() {
        space {
            add(' ', 4)
            add("\u200C", 0)
        }

        val blank = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/mob_types_blank.png"))
        val left = blank.getSubimage(0, 0, 3, 10)
        val middle = blank.getSubimage(3, 0, 6, 10)
        val right = blank.getSubimage(9, 0, 3, 10)

        val font = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/mob_types_font.png"))
        val fontMap: Map<Char, BufferedImage> = ('a'..'z').mapIndexed { index, character ->
            character.uppercaseChar() to font.getSubimage(index * 6, 0, 6, 8)
        }.toMap()

        KnownMobIcon.entries.forEach {
            val name = it.name
            val totalWidth = 6 + name.length * 6
            val image = BufferedImage(totalWidth, 10, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()
            graphics.drawImage(left, 0, 0, null)
            name.forEachIndexed { index, character ->
                graphics.drawImage(middle, 3 + index * 6, 0, null)
                graphics.drawImage(fontMap[character]!!, 3 + index * 6, 1, null)
            }
            graphics.drawImage(right, totalWidth - 3, 0, null)

            graphics.dispose()
            val outputStream = ByteArrayOutputStream()
            val hashingOutputStream = HashingOutputStream(Hashing.sha256(), outputStream)
            ImageIO.write(image, "PNG", hashingOutputStream)
            pngHolder.submit(SkyOcean.id(it.name.lowercase()), outputStream, hashingOutputStream)

            val id = SkyOcean.id("${it.name.lowercase()}.png").withPrefix("$mobTypesTexturePath/")
            bitmap(id, 10, ascent = 8) {
                row(it.icon)
            }
        }
    }

    override fun run(output: CachedOutput): CompletableFuture<*> {
        super.run(output).join()
        return CompletableFuture.allOf(pngHolder.save(output))
    }

    override fun getName() = "Mob Types Font Generator"
}
