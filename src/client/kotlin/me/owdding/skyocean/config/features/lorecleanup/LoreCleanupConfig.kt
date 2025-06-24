package me.owdding.skyocean.config.features.lorecleanup

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object LoreCleanupConfig : CategoryKt("loreCleanup") {
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

    var enableStoragePreview by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.storage_preview"
    }

    var dungeonQuality by boolean(false) {
        this.translation = "skyocean.config.lore_modifiers.dungeon_quality"
    }

}
