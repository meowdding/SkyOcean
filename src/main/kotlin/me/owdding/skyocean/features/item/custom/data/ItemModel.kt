package me.owdding.skyocean.features.item.custom.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.item.custom.ui.standard.search.ItemModelSearchEntry
import me.owdding.skyocean.features.item.custom.ui.standard.search.ModelSearchEntry
import me.owdding.skyocean.features.item.custom.ui.standard.search.SkyBlockModelEntry
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.generated.DispatchHelper
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.item.getVisualItem
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import kotlin.collections.get
import kotlin.reflect.KClass


@GenerateDispatchCodec(ItemModel::class)
enum class ItemModelType(override val type: KClass<out ItemModel>) : DispatchHelper<ItemModel> {
    STATIC(StaticModel::class),
    SKYBLOCK_MODEL(SkyblockModel::class),
    ROTATING(RotatingModel::class),
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
    override fun resolveToItem(): Item? = BuiltInRegistries.ITEM.getOptional(location).orElse(null)
}

@GenerateCodec
data class SkyblockModel(
    val location: SkyBlockId,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.SKYBLOCK_MODEL
    override fun toModelSearchEntry() = SkyBlockModelEntry(location)

    override fun getModel(): Identifier = location.toItem().get(DataComponents.ITEM_MODEL) ?: BuiltInRegistries.ITEM.getKey(location.toItem().item)
    override fun resolveToItem(): Item = location.toItem().getItemModel()
}

data class RotatingModel(
    val items: List<ItemLikeIngredient>,
) : ItemModel {
    override val type: ItemModelType = ItemModelType.ROTATING

    override fun toModelSearchEntry(): ModelSearchEntry? = null

    override fun getModel(): Identifier = items[(TickEvent.ticks / 20) % items.size].let {
        it.id.toItem().get(DataComponents.ITEM_MODEL) ?: BuiltInRegistries.ITEM.getKey(it.id.toItem().item)
    }

    override fun resolveToItem(): Item = items[(TickEvent.ticks / 20) % items.size].id.toItem().getItemModel()
}

interface ItemModel {
    val type: ItemModelType

    fun toModelSearchEntry(): ModelSearchEntry?
    fun getModel(): Identifier
    fun resolveToItem(): Item?
}
