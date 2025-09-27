@file:Suppress("ACTUAL_WITHOUT_EXPECT")

package me.owdding.skyocean.utils.codecs

import com.mojang.serialization.MapCodec
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.contents.KeybindContents
import net.minecraft.network.chat.contents.NbtContents
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.ScoreContents
import net.minecraft.network.chat.contents.SelectorContents
import net.minecraft.network.chat.contents.TranslatableContents

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
