package me.owdding.skyocean.config.features.chat

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

object ChatConfig : CategoryKt("chat") {
    override val name = Translated("skyocean.config.chat")

    var enableProfileInChat by defaultEnabledMessage(
        boolean(true) {
            this.translation = "skyocean.config.chat.profile_in_chat"
        },
        { +"skyocean.config.chat.profile_in_chat.warning" },
        "profile_in_chat",
        predicate = { LocationAPI.isOnSkyBlock },
    )

    var whiteNonMessage by boolean(false) {
        this.searchTerms += "non"
        this.translation = "skyocean.config.chat.white_non_message"
    }

    var piggyRepairHelper by boolean(true) {
        this.translation = "skyocean.config.chat.piggy_repair_helper"
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

    var replyBoop by boolean(true) {
        this.translation = "skyocean.config.chat.reply_boop"
    }
}
