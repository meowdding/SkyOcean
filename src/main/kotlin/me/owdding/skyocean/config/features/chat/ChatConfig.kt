package me.owdding.skyocean.config.features.chat

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.features.chat.ConfigChatPrefix
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

    var replyBoop by boolean(true) {
        this.translation = "skyocean.config.chat.reply_boop"
    }

    var hideBlazetekkMessages by boolean(false) {
        this.translation = "skyocean.config.chat.hide_blazetekk"
    }

    init {
        separator {
            title = "Stylized Chat Prefixes"
        }
    }

    var enableStylizedChatPrefixes by boolean(false) {
        this.searchTerms += ConfigChatPrefix.searchTerms
        this.translation = "skyocean.config.chat.stylized_chat_prefixes"
    }

    var allowedStylizedChatPrefixes by select(*ConfigChatPrefix.default.toTypedArray()) {
        this.searchTerms += ConfigChatPrefix.searchTerms
        this.translation = "skyocean.config.chat.allowed_stylized_chat_prefixes"
    }

    init {
        separator {
            title = "Bridge Formatter"
        }
    }

    var enableBridgeFormatter by boolean(false) {
        this.translation = "skyocean.config.chat.bridge_formatter"
    }

    var bridgeFormatterIgn by string("") {
        this.translation = "skyocean.config.chat.bridge_formatter_ign"
    }

    var bridgeFormatterStylizedChatPrefix by boolean(false) {
        this.translation = "skyocean.config.chat.bridge_formatter_stylized_chat_prefix"
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

    init {
        // Allow for Text replacement config options as well probably
        separator {
            title = "Text Modification"
        }
    }

    var markdownChat by boolean(false) {
        this.searchTerms += "md"
        this.translation = "skyocean.config.chat.markdown_chat"
    }

    var allowUnderscoreItalic by boolean(false) {
        this.translation = "skyocean.config.chat.markdown_chat.underscore_italic"
    }
}
