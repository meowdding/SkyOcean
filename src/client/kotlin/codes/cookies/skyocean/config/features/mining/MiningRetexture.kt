package codes.cookies.skyocean.config.features.mining

import codes.cookies.skyocean.config.requiresChunkRebuild
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object MiningRetexture : CategoryKt("retexture") {
    init {
        category(MineshaftConfig)
    }

    override val name get() = Translated("skyocean.config.mining.retexture")

    var recolorCarpets by requiresChunkRebuild(
        boolean(false) {
            translation = "skyocean.config.mining.retexture.carpets"
        },
    )

    var customGlaciteTextures by requiresChunkRebuild(
        boolean(false) {
            translation = "skyocean.config.mining.retexture.glacite"
        },
    )

    var customMist by requiresChunkRebuild(
        boolean(false) {
            translation = "skyocean.config.mining.retexture.mist"
        },
    )

    var customGemstoneTextures by requiresChunkRebuild(
        boolean(false) {
            translation = "skyocean.config.mining.retexture.gemstone"
        },
    )
}
