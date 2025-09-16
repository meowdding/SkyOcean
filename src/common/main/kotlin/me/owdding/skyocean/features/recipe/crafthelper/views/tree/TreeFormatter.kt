package me.owdding.skyocean.features.recipe.crafthelper.views.tree

import earth.terrarium.olympus.client.components.Widgets
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.features.item.sources.ForgeItemContext
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.RecipeType
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.utils.ChatUtils.append
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Icons
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.extensions.withoutTooltipDelay
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.until
import kotlin.time.Duration.Companion.seconds

object TreeFormatter : RecipeView {
    fun format(
        tree: ContextAwareRecipeTree,
        itemTracker: ItemTracker,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = format(tree, itemTracker, this, widget, widgetConsumer)

    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = append(state, widget, widgetConsumer)

    fun append(state: CraftHelperState, widget: WidgetBuilder, widgetConsumer: (AbstractWidget) -> Unit, depth: Int = 0, prefix: String = "") {
        val parentAmount = if (CraftHelperConfig.parentAmount) {
            state.amountThroughParents
        } else 0

        val needed = (state.required + parentAmount)
        val available = state.amount + parentAmount

        val name = widget.name(state.ingredient)
        if (state.required == 0 && CraftHelperConfig.hideCompleted) {
            return
        }

        val id = (state.ingredient as? SkyOceanItemIngredient)?.skyblockId
        val tooltip = buildList<Component> {
            val sources = state.usedItems.groupBy { it.source }
            fun addUsedSources() {
                if (isNotEmpty()) return
                add(!"Used Item Sources:")
                add(CommonComponents.EMPTY)
            }

            fun addSimple(source: ItemSources, name: String) {
                if (!sources.containsKey(source)) return
                addUsedSources()
                if (state.amountCarryOver != 0 || state.amountThroughParents != 0) {
                    add(CommonComponents.EMPTY)
                }
                add(
                    Text.of(name) {
                        append(": ")
                        append(sources.getValue(source).sumOf { it.amount }.toFormattedString())
                    },
                )
            }

            if (state.amountThroughParents != 0) {
                addUsedSources()
                add(!"Amount through parents: ${state.amountThroughParents.toFormattedString()}")
            }
            if (state.amountCarryOver != 0) {
                addUsedSources()
                add(!"Carry over from previous recipe: ${state.amountCarryOver.toFormattedString()}")
            }

            addSimple(ItemSources.INVENTORY, "Inventory")
            addSimple(ItemSources.SACKS, "Sacks")
            addSimple(ItemSources.STORAGE, "Storage")
            addSimple(ItemSources.WARDROBE, "${Icons.WARDROBE} Wardrobe")
            addSimple(ItemSources.CHEST, "${Icons.CHESTS} Chest")
            addSimple(ItemSources.ACCESSORY_BAG, "${Icons.ACCESSORIES} Accessory Bag")
            addSimple(ItemSources.VAULT, "${Icons.VAULT} Vault")
            addSimple(ItemSources.RIFT, "${Icons.RIFT} Rift")
            addSimple(ItemSources.DRILL_UPGRADE, "${Icons.ITEM_IN_ITEM} Drill Upgrade")
            addSimple(ItemSources.ROD_UPGRADE, "${Icons.ITEM_IN_ITEM} Rod Upgrade")

            if (sources.containsKey(ItemSources.FORGE)) {
                addUsedSources()
                sources.getValue(ItemSources.FORGE).map { it.context }.filterIsInstance<ForgeItemContext>().forEach { context ->
                    val time = context.finishTime.until()
                    val timeDisplay = if (time <= 0.seconds) "Done" else time.toReadableTime()

                    add(!"${Icons.FORGE} Forge Slot: ${context.slot} - $timeDisplay")
                }
            }

            if (id != null && state.recipeType != RecipeType.UNKNOWN) {
                if (this.isNotEmpty()) add(CommonComponents.EMPTY)
                add(!"§eClick to open recipe!")
            }
        }.takeUnless { it.isEmpty() }?.toMutableList()?.let {


            Tooltip.create(
                Text.multiline(it) {
                    this.color = TextColor.GRAY
                },
            )
        }

        val text = Displays.component(
            Text.of {
                append(prefix) { this.color = TextColor.DARK_GRAY }
                append {
                    when {
                        state.isDone() -> {
                            append(Icons.CHECKMARK)
                            this.color = TextColor.GREEN
                            this.bold = true
                        }

                        state.childrenDone -> {
                            append(Icons.WARNING)
                            this.color = TextColor.YELLOW
                        }

                        else -> {
                            append(Icons.CROSS)
                            this.color = TextColor.RED
                            this.bold = true
                        }
                    }
                    append(" ")
                }
                append {
                    append(available.toFormattedString())
                    append("/") { color = TextColor.GRAY }
                    append(needed.toFormattedString())

                    this.color = ARGB.lerp(available.toFloat() / needed.toFloat(), TextColor.RED, TextColor.GREEN)
                }

                append(" ")
                append(name)
                append(" ")
                append(widget.getIcons(state.usedItems.map { it.source }))
            },
        )

        widgetConsumer(
            Widgets.button {
                it.withTexture(null)
                it.withSize(text.getWidth(), text.getHeight())
                it.withRenderer(DisplayWidget.displayRenderer(text))
                it.withCallback {
                    id ?: return@withCallback
                    when (state.recipeType) {
                        RecipeType.CUSTOM -> SkyOcean.debug("Custom recipes dont support click actions!")
                        RecipeType.UNKNOWN -> SkyOcean.debug("Clicked unknown recipe type for $id")
                        RecipeType.KAT -> Text.of("No preview yet, go to Kat :(").sendWithPrefix()
                        else -> McClient.sendClientCommand("${state.recipeType.command} $id")
                    }
                }
                it.setTooltip(tooltip)
            }.withoutTooltipDelay(),
        )

        if (state.hasChildren) {
            state.childStates.forEach { childState ->
                append(childState, widget, widgetConsumer, depth + 1, alterPrefix(prefix, childState.isLast))
            }
        }
    }

    fun alterPrefix(prefix: String, isLast: Boolean) = if (prefix.isEmpty()) {
        if (isLast) "└ " else "├ "
    } else {
        prefix.replace("├", "│").replace("└", "  ") + (if (isLast) "└ " else "├ ")
    }
}
