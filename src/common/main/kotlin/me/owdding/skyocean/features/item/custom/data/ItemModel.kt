package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass


@GenerateDispatchCodec(ItemModel::class)
enum class ItemModelType(override val type: KClass<out ItemModel>) : DispatchHelper<ItemModel> {
    STATIC(StaticModel::class),
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

@GenerateCodec
data class StaticModel(
    val location: ResourceLocation,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.STATIC
    override fun getModel() = location
}

interface ItemModel {
    val type: ItemModelType

    fun getModel(): ResourceLocation
}
