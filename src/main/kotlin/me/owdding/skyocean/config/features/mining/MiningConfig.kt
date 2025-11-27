package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.features.mining.ForgeReminder

object MiningConfig : CategoryKt("mining") {

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
    }

    init {
        separator("skyocean.config.mining.hotm")
    }

    var hotmStackSize by boolean(true) {
        translation = "skyocean.config.mining.hotm.stackSize"
    }

    var hotmTotalProgress by boolean(true) {
        translation = "skyocean.config.mining.hotm.totalProgress"
    }

    var hotmDisplayShiftCost by boolean(true) {
        translation = "skyocean.config.mining.hotm.shiftCost"
    }

    var hotmDisplayTotalLeft by boolean(true) {
        translation = "skyocean.config.mining.hotm.totalLeft"
    }

    var hotmReminder by boolean(true) {
        translation = "skyocean.config.mining.hotm.reminder"
    }

    var reminderTitle by boolean(true) {
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
