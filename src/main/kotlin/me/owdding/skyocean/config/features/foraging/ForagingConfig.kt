package me.owdding.skyocean.config.features.foraging

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.helpers.skilltree.SkillTreeConfig


object ForagingConfig : CategoryKt("foraging"), SkillTreeConfig {

    override val name get() = Translated("skyocean.config.foraging")

    override var stackSize by boolean("hotfStackSize", true) {
        translation = "skyocean.config.foraging.hotf.stackSize"
    }

    override var totalProgress by boolean("hotfTotalProgress", true) {
        translation = "skyocean.config.foraging.hotf.totalProgress"
    }

    override var displayShiftCost by boolean("hotfDisplayShiftCost", true) {
        translation = "skyocean.config.foraging.hotf.shiftCost"
    }

    override var displayTotalLeft by boolean("hotfDisplayTotalLeft", true) {
        translation = "skyocean.config.foraging.hotf.totalLeft"
    }

    override var reminder by boolean("hotfReminder", true) {
        translation = "skyocean.config.foraging.hotf.reminder"
    }

    override var reminderTitle by boolean(true) {
        translation = "skyocean.config.foraging.hotf.reminderTitle"
    }

}
