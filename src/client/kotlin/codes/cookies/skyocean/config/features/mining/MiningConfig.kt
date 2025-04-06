package codes.cookies.skyocean.config.features.mining

import codes.cookies.skyocean.config.translation
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object MiningConfig : CategoryKt("mining") {
    init {
        category(MineshaftConfig)
    }

    override val name get() = Translated("skyocean.config.mining")

    var recolorCarpets by boolean(false) {
        translation = "mining.retexture.carpets"
    }

    var customMiningTextures by boolean(false) {
        translation = "mining.retexture.general"
    }

    var customMist by boolean(false) {
        translation = "mining.retexture.mist"
    }
}
