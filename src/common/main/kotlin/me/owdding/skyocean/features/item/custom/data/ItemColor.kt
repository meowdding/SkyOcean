package me.owdding.skyocean.features.item.custom.data

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import earth.terrarium.olympus.client.components.color.type.HsbColor
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.repo.customization.DyeData
import me.owdding.skyocean.utils.Utils.simpleCacheLoader
import net.minecraft.util.Mth
import net.minecraft.world.item.component.DyedItemColor
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@GenerateDispatchCodec(ItemColor::class)
enum class ItemColorType(override val type: KClass<out ItemColor>) : DispatchHelper<ItemColor> {
    STATIC(StaticItemColor::class),
    GRADIENT(GradientItemColor::class),
    SKYBLOCK_DYE(SkyBlockDye::class),
    ANIMATED_SKYBLOCK_DYE(AnimatedSkyBlockDye::class)
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

interface NonSkyblockItemColor : ItemColor

@GenerateCodec
data class StaticItemColor(val colorCode: Int) : NonSkyblockItemColor {
    override val type: ItemColorType = ItemColorType.STATIC

    override fun getColor() = colorCode
}

@GenerateCodec
data class GradientItemColor(
    val gradient: List<Int>,
    val time: Int,
) : NonSkyblockItemColor {
    val actualTime = max(time, gradient.size)
    val colors = buildList {
        val colors = gradient.mapTo(mutableListOf()) { HsbColor.fromRgb(it) }

        val timeForEach = time / colors.size
        colors.addLast(colors.first())

        repeat(actualTime) {
            val current = it / timeForEach
            val progress = it % timeForEach

            if (progress == 0) {
                add(colors[current].withAlpha(255).toRgba())
                return@repeat
            }

            val delta = progress / timeForEach.toFloat()
            val currentColor = colors[current]
            val nextColor = colors.getOrNull(current + 1) ?: currentColor

            val hue = Mth.lerp(delta, currentColor.hue(), nextColor.hue())
            val saturation = Mth.lerp(delta, currentColor.saturation(), nextColor.saturation())
            val brightness = Mth.lerp(delta, currentColor.brightness(), nextColor.brightness())
            add(HsbColor.of(hue, saturation, brightness, 255).toRgba())
        }
    }

    override val type: ItemColorType = ItemColorType.GRADIENT

    override fun getColor(): Int = colors[(TickEvent.ticks + 1) % colors.size]
}

@GenerateCodec
data class GradientEntry(
    val color: Int,
    val progress: Double,
)

@GenerateCodec
data class SkyBlockDye(val id: String) : ItemColor {
    override val type: ItemColorType = ItemColorType.SKYBLOCK_DYE

    init {
        if (DyeData.staticDyes[id] == null) {
            throw IllegalArgumentException("Unknown dye $id")
        }
    }

    val dye = DyeData.staticDyes[id]!!

    override fun getColor(): Int = dye
}

@GenerateCodec
data class AnimatedSkyBlockDye(val id: String) : ItemColor {
    override val type: ItemColorType = ItemColorType.ANIMATED_SKYBLOCK_DYE

    init {
        if (DyeData.animatedDyes[id] == null) {
            throw IllegalArgumentException("Unknown animated dye $id")
        }
    }

    override fun getColor(): Int = DyeData.getAnimated(id, 1)
}


interface ItemColor {
    val type: ItemColorType

    fun getDyeColor(): DyedItemColor = colorCache[getColor()]
    fun getColor(): Int
}
