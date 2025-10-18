package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.features.mining.mineshaft.MineshaftAnnouncement.ShaftAnnounceType

object MineshaftConfig : CategoryKt("mineshaft") {
    override val name get() = Translated("skyocean.config.mining.mineshaft")

    var mineshaftFoundPity by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.pity_message"
    }

    init {
        separator {
            this.title = "Mineshaft Announcement"
        }
    }

    var shaftAnnouncement by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.announce"
    }

    var shaftAnnounceType by enum(ShaftAnnounceType.PARTY) {
        translation = "skyocean.config.mining.mineshaft.announce-type"
    }

    init {
        separator {
            this.title = "Corpse Waypoint"
        }
    }

    var corpseWaypoint by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.waypoint"
    }

    init {
        separator {
            this.title = "Mineshaft Keys"
        }
    }

    var keyAnnouncement by boolean(true) {
        translation = "skyocean.config.mining.mineshaft.key-announce"
    }


}
