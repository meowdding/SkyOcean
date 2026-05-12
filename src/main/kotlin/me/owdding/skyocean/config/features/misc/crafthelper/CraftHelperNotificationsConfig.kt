package me.owdding.skyocean.config.features.misc.crafthelper

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.config.utils.GenericDropdown.Companion.soundDropdown
import net.minecraft.sounds.SoundEvents

object CraftHelperNotificationsConfig : ObjectKt() {

    var doneTypes by select<CraftHelperNotificationTypes> {
        translation = "skyocean.config.misc.crafthelper.done_notifications.types"
    }

    var soundEvent by soundDropdown(SoundEvents.PLAYER_LEVELUP) {
        translation = "skyocean.config.misc.crafthelper.done_notifications.sound_event"
        condition = { CraftHelperNotificationTypes.DONE_SOUND in doneTypes }
    }
}
