package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.config.withDefaultEnabledMessage
import me.owdding.skyocean.config.withRequiresChunkRebuild
import me.owdding.skyocean.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

object ModelsConfig : ObjectKt(), Translatable {

    var noCarvedPumpkins by boolean(true) {
        this.translation = "config.skyocean.misc.models.noCarvedPumpkins"
    }.withDefaultEnabledMessage(
        { +"skyocean.config.misc.models.noCarvedPumpkins.warning" },
        "noCarvedPumpkins",
        LocationAPI::isOnSkyBlock,
    ).withRequiresChunkRebuild()

    var mutantNetherwartBlock by boolean(false) {
        this.translation = "config.skyocean.misc.models.mutantNetherwartBlock"
    }

    override fun getTranslationKey(): String = "config.skyocean.misc.models"

}
