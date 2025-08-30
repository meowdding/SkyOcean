package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import kotlin.reflect.KClass

@GenerateDispatchCodec(ItemColor::class)
enum class ItemColorType(override val type: KClass<out ItemColor>) : DispatchHelper<ItemColor> {
    STATIC(StaticItemColor::class)
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

@GenerateCodec
data class StaticItemColor(val colorCode: Int) : ItemColor {
    override val type: ItemColorType = ItemColorType.STATIC

    override fun getColor() = colorCode
}

interface ItemColor {
    val type: ItemColorType

    fun getColor(): Int
}
