package me.owdding.skyocean.utils

import com.google.gson.JsonElement
import kotlin.math.max
import kotlin.math.min
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Inline

@GenerateCodec
data class PackMetadata(
    val pack: PackDescriptor,
    var overlays: PackOverlayHolder?,
) {
    fun merge(other: PackMetadata) = PackMetadata(
        pack = pack.merge(other.pack),
        overlays = PackOverlayHolder(listOfNotNull(overlays?.entries, other.overlays?.entries).flatten().toMutableList()).takeUnless { it.entries.isEmpty() },
    )

    fun add(packOverlay: PackOverlay) {
        if (overlays == null) overlays = PackOverlayHolder()
        overlays!!.entries.add(packOverlay)
    }
}

@GenerateCodec
data class PackDescriptor(
    val description: JsonElement,
    @Inline val formats: PackDescriptorFormats,
    @FieldName("pack_format") val packFormat: Int = formats.minFormat,
    @FieldName("supported_formats") val _supportedFormats: OldPackDescriptorFormats = formats.toOldDescriptor(),
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

    fun toOldDescriptor() = OldPackDescriptorFormats(minFormat, maxFormat)
}

@GenerateCodec
data class OldPackDescriptorFormats(
    @FieldName("min_inclusive") val minFormat: Int,
    @FieldName("max_inclusive") val maxFormat: Int,
)

@GenerateCodec
data class PackOverlayHolder(
    val entries: MutableList<PackOverlay> = mutableListOf(),
)

@GenerateCodec
data class PackOverlay(
    val directory: String,
    @Inline val formats: PackDescriptorFormats,
    @FieldName("formats") val _formats: OldPackDescriptorFormats = formats.toOldDescriptor(),
)
