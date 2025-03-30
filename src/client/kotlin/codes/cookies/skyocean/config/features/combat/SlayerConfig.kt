package codes.cookies.skyocean.config.features.combat

import codes.cookies.skyocean.config.translation
import codes.cookies.skyocean.helpers.isGlowing
import codes.cookies.skyocean.mixins.accessor.SlayerAPIAccessor
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerAPI

object SlayerConfig: CategoryKt("slayer") {
    override val name = Translated("skyocean.slayer")

    var enableBlazeHighlight by observable(boolean(true) {
        this.translation = "skyocean.slayer.blaze_highlight"
    }) { oldValue, newValue ->
        (SlayerAPI as SlayerAPIAccessor).slayerBosses.keys.forEach {
            it.isGlowing = false
        }
    }
}
