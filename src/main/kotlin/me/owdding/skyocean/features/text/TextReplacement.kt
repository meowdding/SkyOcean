package me.owdding.skyocean.features.text

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.OptionalBoolean
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import java.util.*

@GenerateCodec
data class TextReplacement(
    val category: UUID?,
    var key: String,
    @NamedCodec("customComponentCodec") var value: Component,
    @OptionalBoolean(true) var enabled: Boolean = true,
    var priority: Int,
    var wholeWord: Boolean = false,
    @FieldName("created_at") val timeCreated: Long = System.currentTimeMillis(),
) {
    val formattedValue: FormattedCharSequence get() = value.visualOrderText
    fun getCategory() = if (category == null) TextReplacementManager.defaultCategory else TextReplacementManager.categories.find {
        it.identifier == category
    } ?: TextReplacementManager.defaultCategory

    fun isEnabled(): Boolean = enabled && getCategory().enabled

}
