package codes.cookies.skyocean.utils.tags

import net.fabricmc.fabric.api.tag.client.v1.ClientTags
import net.minecraft.tags.TagKey

interface AbstractTagKey<T> {

    val key: TagKey<T>
    operator fun contains(element: T): Boolean = ClientTags.isInWithLocalFallback(key, element)

}
