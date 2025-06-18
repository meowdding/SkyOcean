package me.owdding.skyocean.config.features.lorecleanup

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object LoreCleanupConfig : CategoryKt("loreCleanup") {
    override val name = Translated("skyocean.config.lore_cleanup")

    init {
        separator {
            title = "skyocean.config.lore_cleanup.modifications"
            description = "skyocean.config.lore_cleanup.modifications.desc"
        }
    }

    var enableDrillCleanup by boolean(false) {
        this.translation = "skyocean.config.lore_cleanup.drill_modifications"
    }

}
