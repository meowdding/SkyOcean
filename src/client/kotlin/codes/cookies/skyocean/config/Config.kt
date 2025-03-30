package codes.cookies.skyocean.config

import com.teamresourceful.resourcefulconfig.api.annotations.Config
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

@Config("skyocean/config")
@ConfigInfo.Provider(ConfigInfoProvider::class)
object Config : ConfigKt("skyocean/config") {

    var shaftAnnouncement by boolean(false) {
        translation = "skyocean.shaft"
    }

}
