package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.config.duration
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.utils.MinecraftColor
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.transparency
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.Companion.inAnyIsland

object MiscConfig : CategoryKt("misc") {
    private val defaultCloudIslands = listOf(SkyBlockIsland.DWARVEN_MINES, SkyBlockIsland.CRYSTAL_HOLLOWS, SkyBlockIsland.THE_CATACOMBS)
    override val name get() = Translated("skyocean.config.misc")

    var ministerInCalendar by boolean(true) {
        translation = "skyocean.config.misc.ministerInCalendar"
    }

    var previousServer by boolean(false) {
        translation = "skyocean.config.misc.previousServer"
    }

    var previousServerTime by int(360) {
        translation = "skyocean.config.misc.previousServerTime"
    }

    var anvilHelper by boolean(false) {
        translation = "skyocean.config.misc.anvilHelper"
    }

    var hideLightning by boolean(false) {
        translation = "skyocean.config.misc.hideLightning"
    }

    var fullTextShadow by boolean(false) {
        translation = "skyocean.config.misc.fullTextShadow"
    }

    var showHiddenPetCandy by boolean(true) {
        translation = "skyocean.config.misc.showHiddenPetCandy"
    }

    var hideEntityFire by boolean(false) {
        translation = "skyocean.config.misc.hideEntityFire"
    }

    var islandCloudHider by defaultEnabledMessage(
        select(*defaultCloudIslands.toTypedArray()) {
            translation = "skyocean.config.misc.islandCloudHider"
        },
        { +"skyocean.config.misc.islandCloudHider.warning" },
        "islandCloudHider",
        predicate = { inAnyIsland(defaultCloudIslands) },
    )

    val shouldHideClouds get() = SkyBlockIsland.inAnyIsland(islandCloudHider.toList())

    var museumArmourPieces by boolean(true) {
        translation = "skyocean.config.misc.museumArmourPieces"
    }

    var customizationVanillaIntegration by boolean(false) {
        this.translation = "skyocean.config.misc.customization_vanilla_integration"
    }

    init {
        separator("skyocean.config.misc.itemSearch")
    }

    var itemSearchItemHighlight by enum(MinecraftColor.RED) {
        translation = "skyocean.config.misc.itemSearch.itemHighlight"
    }

    var highlightTime by long(10) {
        translation = "skyocean.config.misc.itemSearch.highlightTime"
        slider = true
        range = 10L..60L
    }.duration(SECONDS)

    var preserveLastSearch by boolean(false) {
        translation = "skyocean.config.misc.itemSearch.preserve_search"
    }

    var itemSearchWarpToIsland by boolean(false) {
        translation = "skyocean.config.misc.itemSearch.warp_to_island"
    }

    var itemSearchMuseumIntegration by boolean(false) {
        translation = "skyocean.config.misc.itemSearch.museumIntegration"
    }

    init {
        separator("skyocean.config.misc.transparentArmor")
    }

    var transparentArmorSelf by transparency(100) {
        translation = "skyocean.config.misc.transparentArmor.self"
    }

    var transparentArmorOthers by transparency(100) {
        translation = "skyocean.config.misc.transparentArmor.others"
    }
}

