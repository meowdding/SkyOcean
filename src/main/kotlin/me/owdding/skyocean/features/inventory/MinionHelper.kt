package me.owdding.skyocean.features.inventory

import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutFactory
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.data.profile.MinionStorage
import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.CurrencyType
import me.owdding.skyocean.features.recipe.IngredientType
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.helpers.InventorySideGui
import me.owdding.skyocean.utils.RemoteRepoDelegate
import me.owdding.skyocean.utils.Utils.refreshScreen
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.extensions.asScrollable
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.Pricing
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.containerHeight
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object MinionHelper : InventorySideGui("Crafted Minions") {
    override val enabled: Boolean get() = InventoryConfig.minionHelper

    private const val SCROLLBAR_WIDTH = 10
    val data: MinionRepoData? by RemoteRepoDelegate.load("pv/minions")

    private data class MinionUpgradeInfo(
        val name: String,
        val maxUnlock: Int,
        val missingTiers: Int,
        val price: Long,
        val nextTierId: SkyBlockId,
    )

    override fun ContainerInitializedEvent.getLayout(): Layout? {
        val data = data?.data ?: return null

        val upgrades = MinionStorage.unlockedMinions.mapNotNull { minion ->
            val currentTier = MinionStorage.getMinionTier(minion.partId) ?: return@mapNotNull null
            val maxTier = data.getMax(minion.partId)

            if (maxTier <= 0 || currentTier == maxTier) return@mapNotNull null

            val nextTierId = SkyBlockId.item("${minion.partId}_GENERATOR_${currentTier + 1}")
            val recipe = SimpleRecipeApi.getBestRecipe(nextTierId) ?: return@mapNotNull null

            var price = 0L
            recipe.inputs.forEach { ingredient ->
                when (ingredient.type) {
                    IngredientType.ITEM -> {
                        val id = (ingredient as ItemLikeIngredient).skyblockId
                        if (id.contains("_GENERATOR_")) return@forEach
                        price += Pricing.getPrice(id) * ingredient.amount
                    }

                    IngredientType.CURRENCY -> {
                        if ((ingredient as CurrencyIngredient).currency == CurrencyType.COIN) {
                            price += ingredient.amount
                        }
                    }
                }
            }

            MinionUpgradeInfo(
                name = minion.partId,
                maxUnlock = minion.maxUnlock,
                missingTiers = maxTier - currentTier,
                price = price,
                nextTierId = nextTierId,
            )
        }.ifEmpty { return null }

        return LayoutFactory.vertical {
            val title = LayoutFactory.vertical {
                horizontal {
                    string(ChatUtils.ICON_SPACE_COMPONENT)
                    string(Text.of("Cheapest Minion Upgrades"))
                }
            }
            widget(title)

            LayoutFactory.horizontal {
                spacer(1)
                vertical {
                    spacer(title.width - SCROLLBAR_WIDTH)
                    upgrades.sortedBy { it.price }.forEach { upgrade ->
                        val text = Text.of {
                            append(upgrade.name.toTitleCase(), TextColor.BLUE)
                            append(" ${upgrade.maxUnlock + 1}", TextColor.GREEN)
                            append(": ", TextColor.DARK_GRAY)
                            append(upgrade.price.toFormattedString(), OceanColors.BETTER_GOLD)
                        }
                        button {
                            withSize(McFont.width(text), McFont.height + 2)
                            withTexture(null)
                            withRenderer(WidgetRenderers.text<Button>(text).withShadow())
                            withTooltip(Text.of("Click to set as CraftHelper recipe", TextColor.GRAY))
                            withCallback {
                                CraftHelperStorage.setSelected(upgrade.nextTierId)
                                McScreen.refreshScreen()
                            }
                        }
                    }
                }
            }.let {
                widget(
                    it.asScrollable(
                        it.width + SCROLLBAR_WIDTH, screen.containerHeight - 10 - title.height,
                        {
                            this.withScroll(oldList?.xScroll ?: 0, oldList?.yScroll ?: 0)
                            oldList = this
                        },
                    ),
                )
            }
        }
    }

    @GenerateCodec
    data class MinionRepoData(val data: Levels)

    @GenerateCodec
    data class Levels(@FieldName("default_max") val defaultMax: Int, @FieldName("max_tier_overrides") val maxTierOverrides: Map<String, Int>) {
        fun getMax(id: String) = maxTierOverrides[id] ?: defaultMax
    }
}
