package codes.cookies.skyocean.config.features.chat

import codes.cookies.skyocean.config.translation
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption.Separator
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object ChatConfig: CategoryKt("chat") {
    override val name = Translated("skyocean.config.chat")

    var enableProfileInChat by boolean(true) {
        this.translation = "skyocean.config.chat.profile_in_chat"
    }

    @Separator("Sack Notification")
    private val sackNotificationSeparator = Unit

    var enableSackNotification by boolean(true) {
        this.translation = "skyocean.config.chat.sack_notification"
    }

    var sackNotificationItems by strings("Glossy Gemstone") {
        this.translation = "skyocean.config.chat.sack_notification_items"
    }
}
