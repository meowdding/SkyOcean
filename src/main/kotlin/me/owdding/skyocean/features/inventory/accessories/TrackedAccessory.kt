package me.owdding.skyocean.features.inventory.accessories

import me.owdding.skyocean.data.profile.MarkedAccessoriesStorage
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

    var marked: Boolean
}

data class MissingAccessory(val family: AccessoryFamily) : TrackedAccessory {
    override val familyName: String get() = family.family
    override val items: List<ItemStack> = family.first().map(SimpleItemAPI::getItemById)
    override val type: TrackedAccessoryType = MISSING

    // Missing accessory are always tier 0
    override var marked: Boolean
        get() = MarkedAccessoriesStorage.has(familyName, 0)
        set(value) = MarkedAccessoriesStorage.track(familyName, 0, value)
}

sealed interface HasCurrentTier {
    val currentItem: ItemStack
}

data class UpgradeAccessory(
    val data: AccessoriesHelper.AccessoryUpgradeData,
) : TrackedAccessory, HasCurrentTier {
    override val familyName: String get() = data.family.family
    override val items: List<ItemStack> = data.nextTier.map(SimpleItemAPI::getItemById)
    override val type: TrackedAccessoryType = UPGRADE

    override var marked: Boolean
        get() = MarkedAccessoriesStorage.has(familyName, data.nextTierInt)
        set(value) = MarkedAccessoriesStorage.track(familyName, data.nextTierInt, value)

    override val currentItem get() = data.currentItem
}

data class UpgradeRarityAccessory(
    val data: AccessoriesHelper.AccessoryRarityUpgradeData,
) : TrackedAccessory, HasCurrentTier {
    override val familyName: String get() = data.family.family
    // TODO: handle accessories with the same id but different rarity (pulse ring, book of stats, etc)
    override val items: List<ItemStack> = data.family.first().map(SimpleItemAPI::getItemById)
    override val type: TrackedAccessoryType = UPGRADE

    override var marked: Boolean
        get() = MarkedAccessoriesStorage.has(familyName, 0)
        set(value) = MarkedAccessoriesStorage.track(familyName, 0, value)

    override val currentItem: ItemStack get() = data.currentItem
}
