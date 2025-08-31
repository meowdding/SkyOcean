package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.generated.DispatchHelper
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass


@GenerateDispatchCodec(ItemModel::class)
enum class ItemModelType(override val type: KClass<out ItemModel>) : DispatchHelper<ItemModel> {
    STATIC(StaticModel::class),
    SKYBLOCK_MODEL(SkyblockModel::class)
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

@GenerateCodec
data class SkyblockModel(
    val location: SkyOceanItemId,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.SKYBLOCK_MODEL
    override fun getModel() = BuiltInRegistries.ITEM.getKey(location.toItem().item)
}

interface ItemModel {
    val type: ItemModelType

    fun getModel(): ResourceLocation
}
