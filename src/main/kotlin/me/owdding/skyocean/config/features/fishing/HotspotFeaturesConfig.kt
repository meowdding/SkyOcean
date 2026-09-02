package me.owdding.skyocean.config.features.fishing

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.transparency
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object HotspotFeaturesConfig : ObjectKt() {

    override val baseTranslation: String = "skyocean.config.fishing.hotspot"

    var warning by boolean(false) {
        this.translation = "warning"
    }

    init {
        separator {
            this.title = "highlight"
        }
    }

    var circleSurface by boolean(true) {
        this.translation = "circle_surface"
    }

    var circleOutline by boolean(true) {
        this.translation = "circle_outline"
    }

    var circleColumn by boolean(false) {
        this.translation = "circle_column"
    }

    var circleMatchesPlayerY by boolean(false) {
        this.translation = "circle_matches_player_y"
    }

    var hideParticles by boolean(true) {
        this.translation = "hide_particles"
    }

    var surfaceTransparency by transparency(50) {
        this.translation = "surface_transparency"
    }

    var outlineTransparency by transparency(100) {
        this.translation = "outline_transparency"
    }

    var columnTransparency by transparency(25) {
        this.translation = "column_transparency"
    }

    init {
        separator {
            this.title = "announcements"
        }
    }

    var announce by enum(AnnouncementType.OFF) {
        this.translation = "announcements.announce"
    }

    var chatType by enum(ChatType.PARTY_ONLY) {
        this.translation = "announcements.chat_type"
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
