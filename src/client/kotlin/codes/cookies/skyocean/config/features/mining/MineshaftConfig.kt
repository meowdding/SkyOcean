package codes.cookies.skyocean.config.features.mining

import codes.cookies.skyocean.config.translation
import codes.cookies.skyocean.features.mining.mineshaft.MineshaftAnnouncement.ShaftAnnounceType
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object MineshaftConfig : CategoryKt("mineshaft") {
    override val name = TranslatableValue("skyocean.mining.mineshaft")

    @ConfigOption.Separator("Mineshaft Announcement")
    val announceSeparator = ""

    var shaftAnnouncement by boolean(false) {
        translation = "skyocean.mining.mineshaft.announce"
    }

    var shaftAnnounceType by enum(ShaftAnnounceType.CHAT) {
        translation = "skyocean.mining.mineshaft.announce-type"
    }

    @ConfigOption.Separator("Corpse Waypoint")
    val waypointSeparator = ""

    var corpseWaypoint by boolean(false) {
        translation = "skyocean.mining.mineshaft.waypoint"
    }


}
