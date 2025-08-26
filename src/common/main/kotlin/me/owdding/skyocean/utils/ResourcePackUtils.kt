package me.owdding.skyocean.utils

import com.google.gson.JsonElement
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Unnamed
import kotlin.math.max
import kotlin.math.min

@GenerateCodec
data class PackMetadata(
    val pack: PackDescriptor,
    val overlays: MutableList<PackOverlay> = mutableListOf(),
) {
    fun merge(other: PackMetadata) = PackMetadata(
        pack = pack.merge(other.pack),
        overlays = listOf(overlays, other.overlays).flatten().toMutableList(),
    )
}

@GenerateCodec
data class PackDescriptor(
    val description: JsonElement,
    @Unnamed val formats: PackDescriptorFormats,
    @FieldName("pack_format") val packFormat: Int = formats.minFormat,
    @FieldName("supported_formats") val _supportedFormats: PackDescriptorFormats = formats,
) {
    fun merge(other: PackDescriptor): PackDescriptor = PackDescriptor(
        description = description,
        formats = formats.merge(other.formats),
    )
}

@GenerateCodec
data class PackDescriptorFormats(
    @FieldName("min_format") val minFormat: Int,
    @FieldName("max_format") val maxFormat: Int,
) {
    fun merge(other: PackDescriptorFormats) = PackDescriptorFormats(
        minFormat = min(minFormat, other.minFormat),
        maxFormat = max(maxFormat, other.maxFormat),
    )
}

@GenerateCodec
data class PackOverlay(
    val directory: String,
    @Unnamed val formats: PackDescriptorFormats,
    @FieldName("formats") val _formats: PackDescriptorFormats = formats,
)
