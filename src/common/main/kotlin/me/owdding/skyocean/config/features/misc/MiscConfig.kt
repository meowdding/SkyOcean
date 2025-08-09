package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.defaultEnabledMessage
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.utils.MinecraftColor
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.transparency
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

object MiscConfig : CategoryKt("misc") {
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

    var showHiddenPetCandy by boolean(true) {
        translation = "skyocean.config.misc.showHiddenPetCandy"
    }

    var hideEntityFire by boolean(false) {
        translation = "skyocean.config.misc.hideEntityFire"
    }

    var islandCloudHider by defaultEnabledMessage(
        select(SkyBlockIsland.DWARVEN_MINES, SkyBlockIsland.CRYSTAL_HOLLOWS) {
            translation = "skyocean.config.misc.islandCloudHider"
        },
        { +"skyocean.config.misc.islandCloudHider.warning" },
        "islandCloudHider",
        predicate = { SkyBlockIsland.inAnyIsland(SkyBlockIsland.DWARVEN_MINES, SkyBlockIsland.CRYSTAL_HOLLOWS) },
    )

    val shouldHideClouds get() = SkyBlockIsland.inAnyIsland(islandCloudHider.toList())

    init {
        separator("skyocean.config.misc.itemSearch")
    }

    var itemSearchItemHighlight by enum(MinecraftColor.RED) {
        translation = "skyocean.config.misc.itemSearch.itemHighlight"
    }

    var preserveLastSearch by boolean(false) {
        translation = "skyocean.config.misc.itemSearch.preserve_search"
    }

    init {
        separator("skyocean.config.misc.crafthelper")
    }

    var craftHelperEnabled by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.enabled"
    }

    var craftHelperHideCompleted by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.hideCompleted"
    }

    var craftHelperParentAmount by boolean(true) {
        translation = "skyocean.config.misc.crafthelper.parentAmount"
    }

    var craftHelperNoRootItems by boolean(false) {
        translation = "skyocean.config.misc.crafthelper.disableRootItems"
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

