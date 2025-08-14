package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.separator
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

object MobIconsConfig : CategoryKt("mob_icons") {

    override val name: TranslatableValue = Translated("skyocean.config.misc.mob_icons")

    var property: Any? = null

    var enabled by boolean(false) {
        this.translation = "skyocean.config.misc.mob_icons.enabled"
    }

    var style by enum(MobIcons.DisplayType.NORMAL) {
        this.translation = "skyocean.config.misc.mob_icons.display_type"
    }

    init {
        separator("skyocean.config.misc.mob_icons.colors")
        KnownMobIcon.entries.forEach {
            observable(
                color(it.name.lowercase(), 0xFFFFFF) {
                    allowAlpha = false
                    name = Translated(it.name.toTitleCase())
                    condition = { MobIcons.getSetting(it).shouldColor }
                },
            ) { _, new ->
                it.color = new.takeUnless { it == 0xFFFFFF }
            }.provideDelegate(this, ::property)

        }
    }

}
