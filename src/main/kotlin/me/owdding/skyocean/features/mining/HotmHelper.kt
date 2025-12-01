package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.lib.repo.CostTypes
import me.owdding.lib.repo.PowderType
import me.owdding.lib.repo.PowderType.*
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.helpers.skilltree.SkillTreeHelper
import me.owdding.skyocean.utils.tags.ItemTagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.item.getVisualItem
import tech.thatgravyboat.skyblockapi.api.profile.hotm.HotmAPI
import tech.thatgravyboat.skyblockapi.api.profile.hotm.HotmData
import tech.thatgravyboat.skyblockapi.api.profile.hotm.HotmPerk
import tech.thatgravyboat.skyblockapi.api.profile.hotm.PowderAPI

@Module
object HotmHelper : SkillTreeHelper<PowderType, HotmData, HotmPerk, HotmAPI>(
    PerkUpgradeStorage.hotm,
    HotmAPI,
    "Heart of the Mountain",
    ItemTagKey.HOTM_PERK_ITEMS,
    MiningConfig,
    CostTypes.POWDER,
    TreeRepoData::hotmByName,
    "Hotm"
) {

    // We default to null even if the `when` statement is currently exhaustive, since hypixel could add more powder types in the future
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun PowderType.getCurrentAmount(): Long? = when (this) {
        MITHRIL -> PowderAPI.mithril
        GEMSTONE -> PowderAPI.gemstone
        GLACITE -> PowderAPI.glacite
        else -> null
    }

    override fun ItemStack.isLocked(): Boolean = item.getVisualItem() == Items.COAL
}
