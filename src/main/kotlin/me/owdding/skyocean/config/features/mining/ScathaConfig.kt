package me.owdding.skyocean.config.features.mining

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object ScathaConfig : CategoryKt("scatha") {
    override val name get() = Translated("skyocean.config.mining.scatha")
    var wormAnnouncer by boolean(true) {
        translation = "skyocean.config.mining.scatha.wormAnnouncer"
    }

    var wormCooldown by boolean(true) {
        translation = "skyocean.config.mining.scatha.wormCooldown"
    }

    var replacePetMessage by boolean(true) {
        translation = "skyocean.config.mining.scatha.wormPetMessage"
    }

    var petDropTitle by boolean(true) {
        translation = "skyocean.config.mining.scatha.wormPetTitle"
    }
}
