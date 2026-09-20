package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.utils.items.ItemStackBlueprint
import me.owdding.skyocean.utils.Utils.previous
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.get

fun ItemLike.getEquipmentSlot() = asItem().components().get(DataComponents.EQUIPPABLE)?.slot()

@Suppress("DEPRECATION")
operator fun Item.contains(item: ItemStackBlueprint) = this.builtInRegistryHolder().`is`(item.item)

fun Item.asBlueprint() = ItemStackBlueprint(this)
fun ItemStack.asBlueprint() = ItemStackBlueprint(this.typeHolder(), this.count, this.componentsPatch)

fun ItemStack.getRealRarity(): SkyBlockRarity? {
    var rarity = this[DataTypes.RARITY] ?: return null
    if (this[DataTypes.RECOMBOBULATOR] == true) rarity = rarity.previous() ?: return rarity
    // TODO: get max dungeon quality from repo maybe?
    if (this[DataTypes.DUNGEON_QUALITY] == 50) rarity = rarity.previous() ?: return rarity
    return rarity
}

fun ItemLike.model(): Identifier = this.asItem().components().get(DataComponents.ITEM_MODEL) ?: BuiltInRegistries.ITEM.getKey(this.asItem())
fun ItemStack.model(): Identifier = this.get(DataComponents.ITEM_MODEL) ?: this.item.model()

fun ItemLike.builder(): ItemBuilder = ItemBuilder().apply {
    this.item = asItem()
}

fun ItemLike.withModel(identifier: Identifier) = this.builder().withModel(identifier).build()

fun ItemBuilder.withModel(identifier: Identifier): ItemBuilder = apply {
    this.set(DataComponents.ITEM_MODEL, identifier)
}
