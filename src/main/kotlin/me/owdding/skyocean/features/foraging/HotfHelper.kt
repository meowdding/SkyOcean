package me.owdding.skyocean.features.foraging

import me.owdding.ktmodules.Module
import me.owdding.lib.repo.CostTypes
import me.owdding.lib.repo.WhisperType
import me.owdding.lib.repo.WhisperType.FOREST
import me.owdding.skyocean.config.features.foraging.ForagingConfig
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.helpers.skilltree.SkillTreeHelper
import me.owdding.skyocean.utils.tags.ItemModelTagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfAPI
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfData
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfPerk
import tech.thatgravyboat.skyblockapi.api.profile.hotf.WhispersAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel

@Module
object HotfHelper : SkillTreeHelper<WhisperType, HotfData, HotfPerk, HotfAPI>(
    PerkUpgradeStorage.hotf,
    HotfAPI,
    "Heart of the Forest",
    ItemModelTagKey.HOTF_PERK_ITEMS,
    ForagingConfig,
    CostTypes.WHISPER,
) {

    // We default to null even if the `when` statement is currently exhaustive, since hypixel could add more whisper types in the future
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun WhisperType.getCurrentAmount(): Long? = when (this) {
        FOREST -> WhispersAPI.forest
        else -> null
    }

    override fun ItemStack.isLocked() = item.getItemModel() == Items.PALE_OAK_BUTTON
}

