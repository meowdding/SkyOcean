package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.accessors.ItemStackAttachmentAccessor
import me.owdding.skyocean.utils.items.ItemAttachmentKey
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

fun ItemLike.getEquipmentSlot() = asItem().components().get(DataComponents.EQUIPPABLE)?.slot()
operator fun <T> ItemStack.get(key: ItemAttachmentKey<T>) = (this as ItemStackAttachmentAccessor).`skyocean$getData`(key)
operator fun <T> ItemStack.set(key: ItemAttachmentKey<T>, value: T) = (this as ItemStackAttachmentAccessor).`skyocean$setData`(key, value)
fun <T> ItemStack.hasKey(key: ItemAttachmentKey<T>) = (this as ItemStackAttachmentAccessor).`skyocean$hasKey`(key)
fun <T> ItemStack.remove(key: ItemAttachmentKey<T>): T? = (this as ItemStackAttachmentAccessor).`skyocean$remove`(key)
fun ItemStack.getAttachments() = (this as ItemStackAttachmentAccessor).`skyocean$getAll`()
fun ItemStack.putAttachments(map: Map<ItemAttachmentKey<*>, Any>) = (this as ItemStackAttachmentAccessor).`skyocean$putAll`(map)

