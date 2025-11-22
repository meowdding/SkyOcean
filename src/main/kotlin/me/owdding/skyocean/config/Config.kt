package me.owdding.skyocean.config

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.chat.ChatConfig
import me.owdding.skyocean.config.features.combat.SlayerConfig
import me.owdding.skyocean.config.features.dungeons.DungeonsConfig
import me.owdding.skyocean.config.features.fishing.FishingConfig
import me.owdding.skyocean.config.features.foraging.ForagingConfig
import me.owdding.skyocean.config.features.foraging.GalateaConfig
import me.owdding.skyocean.config.features.garden.GardenConfig
import me.owdding.skyocean.config.features.inventory.Buttons
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.config.features.mining.MiningRetexture
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.config.features.misc.MobIconsConfig
import me.owdding.skyocean.config.features.misc.`fun`.FunConfig
import me.owdding.skyocean.config.hidden.OverlayPositions
import me.owdding.skyocean.config.patcher.ConfigPatches
import me.owdding.skyocean.utils.SkyOceanModifyIndicator
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.OceanGradients
import java.util.function.UnaryOperator

object Config : ConfigKt("skyocean/config") {

    override val name: TranslatableValue = TranslatableValue("SkyOcean")
    override val description: TranslatableValue = TranslatableValue("SkyOcean (v${SkyOcean.VERSION})")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    init {
        category(ChatConfig)
        category(SlayerConfig)
        category(FishingConfig)
        category(ForagingConfig) {
            category(GalateaConfig)
        }
        category(GardenConfig)
        category(InventoryConfig)
        category(LoreModifierConfig)
        category(MiningConfig) {
            category(MiningRetexture)
            category(MineshaftConfig)
        }
        category(DungeonsConfig)
        category(MiscConfig) {
            category(MobIconsConfig)
            category(CraftHelperConfig)
            category(FunConfig)
        }
        category(Buttons)
        category(OverlayPositions)

        separator("skyocean.config.main.modifications")
    }

    val disableMessageTextShadow: Boolean by invalidProperty(
        boolean(true) {
            translation = "skyocean.config.main.text_shadow"
        },
        ChatUtils.prefixDelegate,
    )

    val prefixGradient: OceanGradients by invalidProperty(
        enum(OceanGradients.DEFAULT) {
            translation = "skyocean.config.main.prefix_gradient"
        },
        ChatUtils.prefixDelegate,
    )

    val clickablePrefix: Boolean by invalidProperty(
        boolean(true) {
            translation = "skyocean.config.main.clickable_prefix"
        },
        ChatUtils.prefixDelegate,
    )

    val modifyIndicator by enum(SkyOceanModifyIndicator.PREFIX) {
        translation = "skyocean.config.main.modify_indicator"
    }

    override val patches: Map<Int, UnaryOperator<JsonObject>> = ConfigPatches.loadPatches()
    override val version: Int = patches.size + 1

    fun save() = SkyOcean.config.save()
}
