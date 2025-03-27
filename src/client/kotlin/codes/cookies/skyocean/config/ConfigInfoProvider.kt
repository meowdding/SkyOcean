package codes.cookies.skyocean.config

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColorValue
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import net.fabricmc.loader.api.FabricLoader

object ConfigInfoProvider: ResourcefulConfigInfo {

    private val self = FabricLoader.getInstance().getModContainer("skyocean").get()

    override fun title(): TranslatableValue = TranslatableValue("SkyOcean")
    override fun description(): TranslatableValue = TranslatableValue("SkyOcean (v${self.metadata.version.friendlyString})")
    override fun icon(): String = "box"
    override fun color(): ResourcefulConfigColor = ResourcefulConfigColorValue.create("#FFFF00")
    override fun links(): Array<ResourcefulConfigLink> = emptyArray()
    override fun isHidden() = false
}