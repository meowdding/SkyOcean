package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemModelSearchEntry
import me.owdding.skyocean.features.item.custom.ui.standard.search.ModelSearchEntry
import me.owdding.skyocean.features.item.custom.ui.standard.search.SkyBlockModelEntry
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.utils.extensions.model
import me.owdding.skyocean.utils.extensions.withModel
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import kotlin.reflect.KClass


@GenerateDispatchCodec(ItemModel::class)
enum class ItemModelType(override val type: KClass<out ItemModel>) : DispatchHelper<ItemModel> {
    STATIC(StaticModel::class),
    SKYBLOCK_MODEL(SkyblockModel::class),
    ;

    companion object {
        fun getType(id: String) = entries.firstOrNull { it.id.equals(id, true) } ?: throw UnsupportedOperationException("Unknown type $id")
    }
}

@GenerateCodec
data class StaticModel(
    val location: Identifier,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.STATIC
    override fun toModelSearchEntry() = ItemModelSearchEntry(location)

    override fun getModel() = location
    override fun resolveToItem(): ItemStack = BuiltInRegistries.ITEM.getOptional(location).map { it.defaultInstance }.orElseGet {
        Items.PAPER.withModel(location)
    }
}

@GenerateCodec
data class SkyblockModel(
    val location: SkyBlockId,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.SKYBLOCK_MODEL
    override fun toModelSearchEntry() = SkyBlockModelEntry(location)

    override fun getModel() = location.toItem().model()
    override fun resolveToItem(): ItemStack = location.toItem()
}

interface ItemModel {
    val type: ItemModelType

    fun toModelSearchEntry(): ModelSearchEntry
    fun getModel(): Identifier
    fun resolveToItem(): ItemStack?
}
