package me.owdding.skyocean.config.overlays

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable

// This is because we need to use the property delegate in the init block
private val EMPTY_PROPERTY = object {}

open class OverlayConfig(private val title: String) : ObjectKt(), Translatable {
    override fun getTranslationKey(): String = this.title
}
