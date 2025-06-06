package me.owdding.skyocean.config

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.ktmodules.AutoCollect
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.patcher.ConfigPatches
import me.owdding.skyocean.generated.SkyOceanConfigCategories
import java.util.function.UnaryOperator

object Config : ConfigKt("skyocean/config") {

    override val name: TranslatableValue = TranslatableValue("SkyOcean")
    override val description: TranslatableValue = TranslatableValue("SkyOcean (v${SkyOcean.VERSION})")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    init {
        SkyOceanConfigCategories.collected.forEach { category(it) }
        separator {
            title = "skyocean.config.main.modifications"
            description = "skyocean.config.main.modifications.desc"
        }
    }

    override val patches: Map<Int, UnaryOperator<JsonObject>> = ConfigPatches.loadPatches()
    override val version: Int = patches.size + 1
}

@AutoCollect("ConfigCategories")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ConfigCategory
