package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object HotspotFeaturesConfig : ObjectKt() {

    const val LANG_PATH = "skyocean.config.fishing.hotspot"

    var warning by boolean(false) {
        this.translation = "$LANG_PATH.warning"
    }

    init {
        separator {
            this.title = "$LANG_PATH.highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "$LANG_PATH.circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "$LANG_PATH.circle_outline"
    }

    var circleColumn by boolean(false) {
        this.translation = "$LANG_PATH.circle_column"
    }

    var circleMatchesPlayerY by boolean(false) {
        this.translation = "$LANG_PATH.circle_matches_player_y"
    }

    var hideParticles by boolean(true) {
        this.translation = "$LANG_PATH.hide_particles"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "$LANG_PATH.surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "$LANG_PATH.outline_transparency"
    }

    var columnTransparency by transparency(25) {
        this.translation = "$LANG_PATH.column_transparency"
    }

    init {
        separator {
            this.title = "$LANG_PATH.announcements"
        }
    }

    var announce by enum(AnnouncementType.OFF) {
        this.translation = "$LANG_PATH.announcements.announce"
    }

    var chatType by enum(ChatType.PARTY_ONLY) {
        this.translation = "$LANG_PATH.announcements.chat_type"
    }

    enum class AnnouncementType {
        OFF,
        MANUAL,
        AUTOMATIC,
        ;
    }

    enum class ChatType(val announce: (String) -> Unit) {
        PARTY_ONLY(
            { chat ->
                if (PartyAPI.inParty) {
                    McClient.sendCommand("pc $chat")
                }
            },
        ),
        CURRENT({ McClient.connection?.sendChat(it) }),
        ;
    }
}
