package me.owdding.skyocean.config.features.lorecleanup

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.features.item.lore.DyeHexLoreModifier

object LoreModifierConfig : CategoryKt("lore_modifiers") {
    override val name = Translated("skyocean.config.lore_modifiers")

    init {
        separator {
            title = "skyocean.config.lore_modifiers.modifications"
            description = "skyocean.config.lore_modifiers.modifications.desc"
        }
    }

    var enableDrillCleanup by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.drill_modifications"
    }

    var dungeonQuality by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.dungeon_quality"
    }

    var compactLevelBars by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.compact_level_bars"
    }

    var prehistoryEggBlocksWalked by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.armadillo_blocks_walked"
    }

    var enableStoragePreview by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.storage_preview"
    }

    var museumDonation by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.museum_donation"
    }

    var dyeHex by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.dye_hex"
    }

    var dyePosition by enum(DyeHexLoreModifier.DyePosition.LEFT) {
        this.translation = "skyocean.config.lore_modifiers.dye_pos"
        searchTerms += listOf("left", "middle", "right")
    }

    var midasBidBreakdown by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.midas_bid_breakdown"
    }
}
