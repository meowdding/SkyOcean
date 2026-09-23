package me.owdding.skyocean.datagen.font

import me.owdding.skyocean.utils.Utils
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import javax.imageio.ImageIO

const val smallFontWidth = 5
const val smallFontHeight = 5

fun getSmallFont(): Map<Char, BufferedImage> {
    val font = ImageIO.read(Utils.loadFromResourcesAsStream("data/skyocean/textures/small.png"))
    val fontMap: Map<Char, BufferedImage> = (('A'..'Z') + '-').mapIndexed { index, character ->
        character to font.getSubimage(index * smallFontWidth, 0, smallFontWidth, smallFontHeight)
    }.toMap()
    return fontMap
}

fun Graphics2D.drawImageTinted(
    image: BufferedImage,
    x: Int,
    y: Int,
    tint: Color,
) {
    val tintedImage = RescaleOp(
        tint.getRGBComponents(null),
        FloatArray(4) { 0f },
        null,
    ).filter(image, null)
    drawImage(tintedImage, x, y, null)
}
