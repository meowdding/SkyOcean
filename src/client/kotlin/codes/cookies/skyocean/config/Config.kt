package codes.cookies.skyocean.config

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo
import com.teamresourceful.resourcefulconfig.api.annotations.Config
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry

@Config("skyocean/config")
@ConfigInfo.Provider(ConfigInfoProvider::class)
object Config {

    @ConfigEntry(id = "test")
    var enable: Boolean = true

}