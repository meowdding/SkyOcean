package codes.cookies.skyocean.config.features.chat

import codes.cookies.skyocean.config.translation
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object ChatConfig: CategoryKt("chat") {
    override val name = Translated("skyocean.config.chat")

    var enableProfileInChat by boolean(true) {
        this.translation = "skyocean.config.chat.profile_in_chat"
    }
}
