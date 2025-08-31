package me.owdding.skyocean.features.item.custom.data

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.utils.Utils.simpleCacheLoader
import net.minecraft.world.item.component.DyedItemColor
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@GenerateDispatchCodec(ItemColor::class)
enum class ItemColorType(override val type: KClass<out ItemColor>) : DispatchHelper<ItemColor> {
    STATIC(StaticItemColor::class)
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

val colorCache: LoadingCache<Int, DyedItemColor> = CacheBuilder.newBuilder()
    .maximumSize(500)
    .expireAfterAccess(10.minutes.toJavaDuration())
    .expireAfterWrite(10.minutes.toJavaDuration())
    .build(simpleCacheLoader(::DyedItemColor))

@GenerateCodec
data class StaticItemColor(val colorCode: Int) : ItemColor {
    override val type: ItemColorType = ItemColorType.STATIC

    override fun getColor() = colorCode
}

interface ItemColor {
    val type: ItemColorType

    fun getDyeColor(): DyedItemColor = colorCache[getColor()]
    fun getColor(): Int
}
