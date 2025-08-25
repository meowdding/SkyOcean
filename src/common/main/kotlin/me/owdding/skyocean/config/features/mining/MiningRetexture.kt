package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.requiresChunkRebuild

object MiningRetexture : CategoryKt("retexture") {

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

    var customHollowTextures by requiresChunkRebuild(
        boolean(false) {
            translation = "skyocean.config.mining.retexture.hollows"
        },
    )
}
