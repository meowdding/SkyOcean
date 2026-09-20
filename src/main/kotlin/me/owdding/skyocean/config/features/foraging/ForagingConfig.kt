package me.owdding.skyocean.config.features.foraging

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.duration
import kotlin.time.DurationUnit

object ForagingConfig : CategoryKt("foraging") {

    override val name get() = Translated("skyocean.config.foraging")

    var hotfStackSize by boolean(true) {
        translation = "skyocean.config.foraging.hotf.stackSize"
    }

    var hotfTotalProgress by boolean(true) {
        translation = "skyocean.config.foraging.hotf.totalProgress"
    }

    var hotfDisplayShiftCost by boolean(true) {
        translation = "skyocean.config.foraging.hotf.shiftCost"
    }

    var hotfDisplayTotalLeft by boolean(true) {
        translation = "skyocean.config.foraging.hotf.totalLeft"
    }

    var hotfReminder by boolean(true) {
        translation = "skyocean.config.foraging.hotf.reminder"
    }

    var reminderTitle by boolean(true) {
        translation = "skyocean.config.foraging.hotf.reminderTitle"
    }

    var hotfRepeatReminder by boolean(false) {
        translation = "skyocean.config.foraging.hotf.repeatReminder"
    }
    var hotfReminderInterval by long(30) {
        translation = "skyocean.config.foraging.hotf.reminderInterval"
        slider = true
        range = 5L..180L
    }.duration(DurationUnit.SECONDS)

}
