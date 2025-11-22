package me.owdding.skyocean.datagen.providers

import com.mojang.blaze3d.font.SpaceProvider
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.gui.font.providers.BitmapProvider
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture
import kotlin.streams.toList

val CODEC = RecordCodecBuilder.create {
    it.group(
        GlyphProviderDefinition.MAP_CODEC.codec().listOf().fieldOf("providers").forGetter { it },
    ).apply(it) { it }
}

abstract class SkyOceanFontProvider(val output: PackOutput, val id: ResourceLocation) : DataProvider {
    val fontPathProvider: PackOutput.PathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "font")

    abstract fun SkyOceanFontProviderHolder.create()

    override fun run(output: CachedOutput): CompletableFuture<*> {
        val holder = SkyOceanFontProviderHolder(mutableListOf())
        holder.create()
        return DataProvider.saveAll(output, CODEC, { fontPathProvider.json(it) }, mapOf(id to holder.glyphProvider))
    }

    data class SkyOceanFontProviderHolder(
        val glyphProvider: MutableList<GlyphProviderDefinition>,
    ) {
        fun space(init: SpaceProviderDefinitionBuilder.() -> Unit) {
            glyphProvider.add(SpaceProviderDefinitionBuilder().apply(init).build())
        }

        fun bitmap(file: ResourceLocation, height: Int, ascent: Int = 7, init: BitMapProviderDefinitionBuilder.() -> Unit) {
            glyphProvider.add(BitMapProviderDefinitionBuilder(file, height, ascent).apply(init).build())
        }
    }

    interface SkyOceanProviderDefinitionBuilder {
        fun build(): GlyphProviderDefinition
    }

    data class BitMapProviderDefinitionBuilder(
        val file: ResourceLocation,
        val height: Int,
        val ascent: Int,
        val grid: MutableList<List<Int>> = mutableListOf(),
    ) : SkyOceanProviderDefinitionBuilder {
        fun row(row: String) {
            grid.add(row.codePoints().toList())
        }

        fun row(init: MutableList<String>.() -> Unit) {
            val list = mutableListOf<String>()
            list.init()
            grid.add(list.map { it.codePoints().findFirst().orElseThrow() })
        }

        override fun build(): GlyphProviderDefinition = BitmapProvider.Definition(
            file, height, ascent, grid.map { it.toIntArray() }.toTypedArray(),
        )
    }

    data class SpaceProviderDefinitionBuilder(private val holder: MutableMap<String, Int> = mutableMapOf()) : SkyOceanProviderDefinitionBuilder {
        fun add(char: Char, space: Int) {
            holder[char.toString()] = space
        }

        fun add(char: String, space: Int) {
            holder[char] = space
        }

        override fun build(): GlyphProviderDefinition = SpaceProvider.Definition(
            holder.mapKeys { (k) -> k.codePoints().findFirst().orElseThrow() }.mapValues { (_, v) -> v.toFloat() },
        )
    }
}
