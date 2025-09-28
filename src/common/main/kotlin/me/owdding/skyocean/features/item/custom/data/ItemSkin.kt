package me.owdding.skyocean.features.item.custom.data

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.mojang.authlib.properties.Property
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.repo.customization.AnimatedSkulls
import me.owdding.skyocean.utils.Utils.simpleCacheLoader
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.platform.ResolvableProfile
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@GenerateDispatchCodec(ItemSkin::class)
enum class ItemSkinType(override val type: KClass<out ItemSkin>) : DispatchHelper<ItemSkin> {
    STATIC(StaticSkin::class),
    SKYBLOCK_SKIN(SkyblockSkin::class),
    ANIMATED_SKYBLOCK_SKIN(AnimatedSkyblockSkin::class),
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

val skinCache: LoadingCache<String, ResolvableProfile> = CacheBuilder.newBuilder()
    .maximumSize(500)
    .expireAfterAccess(10.minutes.toJavaDuration())
    .expireAfterWrite(10.minutes.toJavaDuration())
    .build(
        simpleCacheLoader { skin ->
            ResolvableProfile {
                put("textures", Property("textures", skin))
            }
        },
    )

@GenerateCodec
data class StaticSkin(
    val skin: String,
) : ItemSkin {
    override val type: ItemSkinType = ItemSkinType.STATIC

    override fun getResolvableProfile(): ResolvableProfile = skinCache[skin]
}

@GenerateCodec
data class SkyblockSkin(
    val item: SkyBlockId,
) : ItemSkin {
    override val type: ItemSkinType = ItemSkinType.SKYBLOCK_SKIN

    override fun getResolvableProfile(): ResolvableProfile? = item.toItem().get(DataComponents.PROFILE)
}

@GenerateCodec
data class AnimatedSkyblockSkin(
    val id: SkyBlockId,
) : ItemSkin {
    val skin = AnimatedSkulls.skins[id] ?: throw RuntimeException("Failed to get animated skyblock skin $id", NullPointerException())
    override val type: ItemSkinType = ItemSkinType.ANIMATED_SKYBLOCK_SKIN

    override fun getResolvableProfile(): ResolvableProfile? = skin.let { skinCache[it.getTexture()] }

}

interface ItemSkin {
    val type: ItemSkinType

    fun getResolvableProfile(): ResolvableProfile?
}
