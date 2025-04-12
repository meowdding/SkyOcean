package codes.cookies.skyocean.config.features.mining

import codes.cookies.skyocean.config.translation
import codes.cookies.skyocean.features.mining.mineshaft.MineshaftAnnouncement.ShaftAnnounceType
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object MineshaftConfig : CategoryKt("mineshaft") {
    override val name get() = Translated("skyocean.config.mining.mineshaft")

    @ConfigOption.Separator("Mineshaft Announcement")
    val announceSeparator = ""

    var shaftAnnouncement by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.announce"
    }

    var shaftAnnounceType by enum(ShaftAnnounceType.CHAT) {
        translation = "skyocean.config.mining.mineshaft.announce-type"
    }

    @ConfigOption.Separator("Corpse Waypoint")
    val waypointSeparator = ""

    var corpseWaypoint by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.waypoint"
    }

    @ConfigOption.Separator("Mineshaft Keys")
    val keysSeparator = ""

    var keyAnnouncement by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.key-announce"
    }


}
