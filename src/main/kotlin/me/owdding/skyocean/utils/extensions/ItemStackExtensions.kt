package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.utils.Utils.previous
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.extentions.get

fun ItemLike.getEquipmentSlot() = asItem().components().get(DataComponents.EQUIPPABLE)?.slot()

fun ItemStack.getRealRarity(): SkyBlockRarity? {
    var rarity = this[DataTypes.RARITY] ?: return null
    if (this[DataTypes.RECOMBOBULATOR] == true) rarity = rarity.previous() ?: return rarity
    // TODO: get max dungeon quality from repo maybe?
    if (this[DataTypes.DUNGEON_QUALITY] == 50) rarity = rarity.previous() ?: return rarity
    return rarity
}
