@file:Suppress("ACTUAL_WITHOUT_EXPECT")

package me.owdding.skyocean.utils.codecs

import com.mojang.serialization.MapCodec
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.contents.*
import net.minecraft.resources.ResourceLocation

private val componentTypes = arrayOf(
    PlainTextContents.TYPE,
    TranslatableContents.TYPE,
    KeybindContents.TYPE,
    ScoreContents.TYPE,
    SelectorContents.TYPE,
    NbtContents.TYPE,
)

internal actual fun createContentCodec(): MapCodec<ComponentContents> = ComponentSerialization.createLegacyComponentMatcher(
    componentTypes,
    ComponentContents.Type<*>::codec,
    { it!!.type() },
    "type",
)

internal actual fun toClientAsset(resourceLocation: ResourceLocation): ClientAsset = ClientAsset(resourceLocation.withPath { "textures/$it.png" })
internal actual fun fromClientAsset(asset: ClientAsset): ResourceLocation = asset.id.withPath { it.removeSurrounding("textures/", ".png") }
