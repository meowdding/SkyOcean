package me.owdding.skyocean.features.inventory.accessories

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SimpleItemAPI

enum class TrackedAccessoryType {
    MISSING,
    UPGRADE,
    ALL,
    ;

    fun matches(type: TrackedAccessoryType): Boolean {
        if (type == ALL || this == ALL) return true
        return this == type
    }
}

sealed interface TrackedAccessory {
    val familyName: String
    val items: List<ItemStack>
    val type: TrackedAccessoryType
}

open class MissingAccessory(val family: AccessoryFamily) : TrackedAccessory {
    override val familyName: String get() = family.family
    override val items: List<ItemStack> = family.first().map(SimpleItemAPI::getItemById)
    override val type: TrackedAccessoryType = TrackedAccessoryType.MISSING
}

open class UpgradeAccessory(
    val data: AccessoriesHelper.AccessoryUpgradeData,
) : TrackedAccessory {
    override val familyName: String get() = data.family.family
    override val items: List<ItemStack> = data.nextTier.map(SimpleItemAPI::getItemById)
    override val type: TrackedAccessoryType = TrackedAccessoryType.UPGRADE

    val currentItem get() = data.currentItem
}
