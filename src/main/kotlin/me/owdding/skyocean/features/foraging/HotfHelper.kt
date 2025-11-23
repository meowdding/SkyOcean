package me.owdding.skyocean.features.foraging

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.foraging.ForagingConfig
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.helpers.skilltree.SkillTreeHelper
import me.owdding.skyocean.utils.tags.ItemModelTagKey
import tech.thatgravyboat.skyblockapi.api.profile.hotf.*
import tech.thatgravyboat.skyblockapi.api.profile.hotf.WhisperType.FOREST

@Module
object HotfHelper : SkillTreeHelper<WhisperType, HotfData, HotfPerk, HotfAPI>(
    PerkUpgradeStorage.hotf,
    HotfAPI,
    "Heart of the Forest",
    ItemModelTagKey.HOTF_PERK_ITEMS,
    ForagingConfig,
) {

    // We default to null even if the `when` statement is currently exhaustive, since hypixel could add more whisper types in the future
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun WhisperType.getCurrentAmount(): Long? = when (this) {
        FOREST -> WhispersAPI.forest
        else -> null
    }
}

