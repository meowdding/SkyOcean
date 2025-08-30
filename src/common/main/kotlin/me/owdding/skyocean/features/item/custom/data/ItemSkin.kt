package me.owdding.skyocean.features.item.custom.data

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.utils.Utils.simpleCacheLoader
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@GenerateDispatchCodec(ItemSkin::class)
enum class ItemSkinType(override val type: KClass<out ItemSkin>) : DispatchHelper<ItemSkin> {
    STATIC(StaticSkin::class),
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

val skinCache: LoadingCache<String, GameProfile> = CacheBuilder.newBuilder()
    .maximumSize(500)
    .expireAfterAccess(10.minutes.toJavaDuration())
    .expireAfterWrite(10.minutes.toJavaDuration())
    .build(
        simpleCacheLoader { skin ->
            val profile = GameProfile(UUID.randomUUID(), "a")
            profile.properties.put("textures", Property("textures", skin))
            profile
        },
    )

@GenerateCodec
data class StaticSkin(
    val skin: String,
) : ItemSkin {
    override val type: ItemSkinType = ItemSkinType.STATIC

    override fun getGameProfile(): GameProfile = skinCache[skin]
}

interface ItemSkin {
    val type: ItemSkinType

    fun getGameProfile(): GameProfile
}
