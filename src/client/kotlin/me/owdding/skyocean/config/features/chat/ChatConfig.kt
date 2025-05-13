package me.owdding.skyocean.config.features.chat

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.ConfigCategory

@ConfigCategory
object ChatConfig: CategoryKt("chat") {
    override val name = Translated("skyocean.config.chat")

    var enableProfileInChat by boolean(true) {
        this.translation = "skyocean.config.chat.profile_in_chat"
    }

    init {
        separator {
            title = "Sack Notification"
        }
    }

    var enableSackNotification by boolean(true) {
        this.translation = "skyocean.config.chat.sack_notification"
    }

    var sackNotificationItems by strings("Glossy Gemstone") {
        this.translation = "skyocean.config.chat.sack_notification_items"
    }
}
