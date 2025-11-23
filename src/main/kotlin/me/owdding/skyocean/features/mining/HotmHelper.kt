package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.helpers.skilltree.SkillTreeHelper
import me.owdding.skyocean.utils.tags.ItemTagKey
import tech.thatgravyboat.skyblockapi.api.profile.hotm.*
import tech.thatgravyboat.skyblockapi.api.profile.hotm.PowderType.*

@Module
object HotmHelper : SkillTreeHelper<PowderType, HotmData, HotmPerk, HotmAPI>(
    PerkUpgradeStorage.hotm,
    HotmAPI,
    "Heart of the Mountain",
    ItemTagKey.HOTM_PERK_ITEMS,
    MiningConfig,
) {

    // We default to null even if the `when` statement is currently exhaustive, since hypixel could add more powder types in the future
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun PowderType.getCurrentAmount(): Long? = when (this) {
        MITHRIL -> PowderAPI.mithril
        GEMSTONE -> PowderAPI.gemstone
        GLACITE -> PowderAPI.glacite
        else -> null
    }
}
