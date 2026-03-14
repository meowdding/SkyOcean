package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.features.mining.ForgeReminder
import me.owdding.skyocean.helpers.skilltree.SkillTreeConfig

object MiningConfig : CategoryKt("mining"), SkillTreeConfig {

    override val name get() = Translated("skyocean.config.mining")

    var modifyCommissions by boolean(true) {
        translation = "skyocean.config.mining.modifyCommissions"
    }

    var chAreaWalls by boolean(false) {
        translation = "skyocean.config.mining.ch.area_walls"
    }

    var puzzlerSolver by boolean(true) {
        translation = "skyocean.config.mining.puzzler"
    }

    var fetchurHelper by boolean(true) {
        translation = "skyocean.config.mining.fetchur"
    }

    var forgeReminder by boolean(true) {
        translation = "skyocean.config.mining.forge_reminder"
    }

    var forgeReminderDelay by int(1) {
        translation = "skyocean.config.mining.forge_reminder_delay"
        slider = true
        range = 1..30
    }

    var forgeReminderAction by enum(ForgeReminder.ForgeReminderAction.WARP) {
        translation = "skyocean.config.mining.forge_reminder_action"
        searchTerms += listOf("call", "fred", "warp")
    }

    init {
        separator("skyocean.config.mining.hotm")
    }

    override var stackSize by boolean("hotmStackSize", true) {
        translation = "skyocean.config.mining.hotm.stackSize"
    }

    override var totalProgress by boolean("hotmTotalProgress", true) {
        translation = "skyocean.config.mining.hotm.totalProgress"
    }

    override var displayShiftCost by boolean("hotmDisplayShiftCost", true) {
        translation = "skyocean.config.mining.hotm.shiftCost"
    }

    override var displayTotalLeft by boolean("hotmDisplayTotalLeft", true) {
        translation = "skyocean.config.mining.hotm.totalLeft"
    }

    override var reminder by boolean("hotmReminder", true) {
        translation = "skyocean.config.mining.hotm.reminder"
    }

    override var reminderTitle by boolean(true) {
        translation = "skyocean.config.mining.hotm.reminderTitle"
    }

    init {
        separator("skyocean.config.mining.metal_detector")
    }

    var metalDetectorSolver by boolean(false) {
        translation = "skyocean.config.mining.metal_detector.metalDetector"
    }

    var playDingOnFind by boolean(true) {
        translation = "skyocean.config.mining.metal_detector.playDingOnFind"
    }

    var showTitleOnFind by boolean(true) {
        translation = "skyocean.config.mining.metal_detector.showTitleOnFind"
    }
}
