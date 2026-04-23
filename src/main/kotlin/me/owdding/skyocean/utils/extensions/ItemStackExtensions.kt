package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.utils.items.ItemStackBlueprint
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

fun ItemLike.getEquipmentSlot() = asItem().components().get(DataComponents.EQUIPPABLE)?.slot()

@Suppress("DEPRECATION")
operator fun Item.contains(item: ItemStackBlueprint) = this.builtInRegistryHolder().`is`(item.item)

fun Item.asBlueprint() = ItemStackBlueprint(this)
//~ if >= 26.1 'getItemHolder' -> 'typeHolder'
fun ItemStack.asBlueprint() = ItemStackBlueprint(this.typeHolder(), this.count, this.componentsPatch)
