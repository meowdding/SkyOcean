package me.owdding.skyocean.utils.extensions

import net.minecraft.core.component.DataComponents
import net.minecraft.world.level.ItemLike

fun ItemLike.getEquipmentSlot() = asItem().components().get(DataComponents.EQUIPPABLE)?.slot()
