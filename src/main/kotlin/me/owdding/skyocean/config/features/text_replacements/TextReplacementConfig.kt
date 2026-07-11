package me.owdding.skyocean.config.features.text_replacements

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.features.text.DisableReplacements
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.helpers.McScreen

object TextReplacementConfig : CategoryKt("text_replacements") {

    override val name get() = Translated("skyocean.config.text_replacements")

    val enabled by boolean(true) {
        this.translation = "skyocean.config.text_replacements.enabled"
    }

    val outsideSkyblock by boolean(true) {
        this.translation = "skyocean.config.text_replacements.outside_skyblock"
    }

    @JvmStatic
    fun isEnabled() = enabled && (outsideSkyblock || LocationAPI.isOnSkyBlock) && McScreen.self !is DisableReplacements

}
