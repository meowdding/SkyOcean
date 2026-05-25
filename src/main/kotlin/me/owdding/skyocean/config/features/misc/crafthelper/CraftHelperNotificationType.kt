package me.owdding.skyocean.config.features.misc.crafthelper

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable

enum class CraftHelperNotificationType : Translatable {
    DONE_MESSAGE,
    DONE_TITLE,
    DONE_SOUND,
    ;

    override fun getTranslationKey() = "skyocean.config.misc.crafthelper.done_notifications.notification_type.${this.name.lowercase()}"
}
