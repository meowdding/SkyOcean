package me.owdding.skyocean.utils.codecs

import me.owdding.ktcodecs.GenerateCodec

@GenerateCodec
data class NamedPosition(
    val x: Double,
    val y: Double,
    val z: Double,
)
