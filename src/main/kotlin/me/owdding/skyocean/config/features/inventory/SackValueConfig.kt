package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt

object SackValueConfig : ObjectKt(), Translatable {
    private const val PATH = "skyocean.config.inventory.sack_value"
    override fun getTranslationKey(): String = "$PATH.edit"

    var enabled by boolean(true) {
        this.translation = PATH
    }

    var priceSource by enum(PriceSource.BAZAAR) {
        this.translation = "$PATH.price_source"
    }

    enum class PriceSource : Translatable {
        BAZAAR,
        NPC,
        ;

        override fun getTranslationKey() = "$PATH.price_source.${this.name.lowercase()}"
    }
}
