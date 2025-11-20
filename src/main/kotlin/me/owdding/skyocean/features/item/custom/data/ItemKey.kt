package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import java.util.*
import kotlin.reflect.KClass


@GenerateDispatchCodec(ItemKey::class)
enum class ItemKeyType(override val type: KClass<out ItemKey>) : DispatchHelper<ItemKey> {
    UUID(UuidKey::class),
    ID_AND_TIME(IdAndTimeKey::class),
    ID(IdKey::class),
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

interface ItemKey {
    val type: ItemKeyType
}

@GenerateCodec
data class UuidKey(val uuid: UUID) : ItemKey {
    override val type: ItemKeyType = ItemKeyType.UUID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UuidKey) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}

@GenerateCodec
data class IdAndTimeKey(
    val item: SkyBlockId,
    val time: Long,
) : ItemKey {
    override val type: ItemKeyType = ItemKeyType.ID_AND_TIME

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdAndTimeKey) return false

        if (time != other.time) return false
        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + item.hashCode()
        return result
    }
}

@GenerateCodec
data class IdKey(
    val key: SkyBlockId,
) : ItemKey {
    override val type: ItemKeyType get() = ItemKeyType.ID
}
