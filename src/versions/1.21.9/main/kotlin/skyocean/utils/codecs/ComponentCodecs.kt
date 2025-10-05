@file:Suppress("ACTUAL_WITHOUT_EXPECT")

package me.owdding.skyocean.utils.codecs

import com.mojang.serialization.MapCodec
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.contents.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs


internal actual fun createContentCodec(): MapCodec<ComponentContents> {
    val idMapper = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out ComponentContents>>()
    idMapper.put("text", PlainTextContents.MAP_CODEC)
    idMapper.put("translatable", TranslatableContents.MAP_CODEC)
    idMapper.put("keybind", KeybindContents.MAP_CODEC)
    idMapper.put("score", ScoreContents.MAP_CODEC)
    idMapper.put("selector", SelectorContents.MAP_CODEC)
    idMapper.put("nbt", NbtContents.MAP_CODEC)
    idMapper.put("object", ObjectContents.MAP_CODEC)

    return ComponentSerialization.createLegacyComponentMatcher(idMapper, ComponentContents::codec, "type")
}

internal actual fun toClientAsset(resourceLocation: ResourceLocation): ClientAsset = ClientAsset.ResourceTexture(resourceLocation)
internal actual fun fromClientAsset(asset: ClientAsset): ResourceLocation = asset.id()
