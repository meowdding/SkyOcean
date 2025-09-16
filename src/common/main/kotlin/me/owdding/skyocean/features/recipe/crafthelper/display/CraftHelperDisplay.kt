package me.owdding.skyocean.features.recipe.crafthelper.display

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.compat.REIRenderOverlayEvent
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asButtonLeft
import me.owdding.lib.displays.withPadding
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.lib.layouts.asWidget
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsCycleElement
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.features.recipe.crafthelper.views.tree.TreeFormatter
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.mixins.FrameLayoutAccessor
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Icons
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.OceanColors
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.extensions.asScrollable
import me.owdding.skyocean.utils.extensions.withoutTooltipDelay
import me.owdding.skyocean.utils.rendering.ExtraDisplays
import me.owdding.skyocean.utils.setPosition
import me.owdding.skyocean.utils.suggestions.CombinedSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeIdSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeNameSuggestionProvider
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.zip.GZIPInputStream
import kotlin.io.encoding.Base64
import kotlin.math.max

@LateInitModule
object CraftHelperDisplay : MeowddingLogger by SkyOcean.featureLogger() {

    val data get() = CraftHelperStorage.data

    private var craftHelperLayout: LayoutElement? = null

    fun clear() {
        CraftHelperStorage.clear()
        CraftHelperStorage.save()
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("recipe") {
            thenCallback("clear") {
                clear()
                Text.of("Cleared current recipe!").sendWithPrefix()
            }
            then("amount", IntegerArgumentType.integer()) {
                callback {
                    val amount = this.getArgument("amount", Int::class.java)
                    if (amount <= 0) {
                        Text.of("Amount must be greater than 0!").withColor(TextColor.RED).sendWithPrefix()
                        return@callback
                    }
                    CraftHelperStorage.setAmount(amount)
                    CraftHelperStorage.save()
                    Text.of("Set current recipe amount to ") {
                        append("$amount") { color = TextColor.GREEN }
                        append("!").sendWithPrefix()
                    }
                }
            }
            thenCallback("skyshards") {
                val clipboard = McClient.clipboard
                try {
                    val base = Base64.decode(clipboard.split(":")[1].trim())
                    val data = GZIPInputStream(base.inputStream()).use { it.readBytes() }.decodeToString()
                        .readJson<JsonObject>().toData(SkyOceanCodecs.SkyShardsMethodCodec.codec())

                    data?.let {
                        val list = mutableListOf<SkyShardsMethod>()
                        it.visitElements(list::add)

                        val containsCycle = list.filterIsInstance<SkyShardsCycleElement>().any()
                        CraftHelperStorage.setSkyShards(it)
                        if (containsCycle) {
                            Text.of("The imported tree contains a cycle, these are currently not supported in skyocean! The tree might not look complete!") {
                                this.color = OceanColors.WARNING
                            }.sendWithPrefix()
                        } else {
                            Text.of("Set current recipe to SkyShards Tree for ") {
                                append("${it.quantity.toFormattedString()}x ") { color = TextColor.GREEN }
                                append(it.shard.toItem().hoverName)
                                append("!")
                            }.sendWithPrefix()
                        }
                    } ?: run {
                        Text.of("Failed to read SkyShards data from clipboard!") { this.color = OceanColors.WARNING }.sendWithPrefix()
                    }
                } catch (e: Exception) {
                    Text.of("Failed to read SkyShards data from clipboard!") { this.color = OceanColors.WARNING }.sendWithPrefix()
                    error("Failed to decode SkyShards tree!", e)
                }
            }
            then("recipe", StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) {
                callback {
                    val input = this.getArgument("recipe", String::class.java)
                    var amount = 1
                    val item = SkyOceanItemId.fromName(input, dropLast = false) ?: SkyOceanItemId.unknownType(input) ?: run {
                        val splitName = input.substringBeforeLast(" ")
                        amount = input.substringAfterLast(" ").toIntOrNull() ?: 1
                        SkyOceanItemId.fromName(splitName) ?: SkyOceanItemId.unknownType(splitName)
                    }
                    CraftHelperStorage.setSelected(item)
                    CraftHelperStorage.setAmount(amount)
                    CraftHelperStorage.save()
                    Text.of("Set current recipe to ") {
                        append("${CraftHelperStorage.selectedAmount}x ") { color = TextColor.GREEN }
                        append(CraftHelperStorage.selectedItem?.toItem()?.let(ItemStack::getHoverName) ?: !"unknown")
                        append("!")
                    }.sendWithPrefix()
                }
            }
        }
    }

    @Subscription
    fun onScreenInit(event: ScreenInitializedEvent) {
        if (!MiscConfig.craftHelperEnabled) return
        //if (!LocationAPI.isOnSkyBlock) return
        if (event.screen !is AbstractContainerScreen<*>) return

        val layout = LayoutFactory.empty() as FrameLayout
        lateinit var callback: (save: Boolean) -> Unit

        fun resetLayout() {
            layout.visitWidgets { event.widgets.remove(it) }
        }
        callback = callback@{ save ->
            val (tree, output) = CraftHelperStorage.data?.resolve(::resetLayout, ::clear) ?: return@callback
            resetLayout()
            (layout as? FrameLayoutAccessor)?.children()?.clear()// todo use clearable layout once #108 is merged
            layout.addChild(visualize(tree, output) { callback })
            layout.arrangeElements()
            layout.setPosition(MiscConfig.craftHelperPosition.position(layout.width, layout.height))
            layout.visitWidgets { event.widgets.add(it) }
            this.craftHelperLayout = layout
            if (save) CraftHelperStorage.save()
        }
        callback(false)
    }

    @Subscription
    fun onREI(event: REIRenderOverlayEvent) {
        craftHelperLayout?.let {
            event.register(it.x, it.y, it.width, it.height)
        }
    }

    @Subscription
    fun onScreenClose(event: ContainerCloseEvent) {
        craftHelperLayout = null
    }

    private fun visualize(tree: ContextAwareRecipeTree, output: ItemLikeIngredient, callback: () -> ((save: Boolean) -> Unit)): AbstractWidget {
        val sources = ItemSources.craftHelperSources - MiscConfig.disallowedCraftHelperSources.toSet()
        val tracker = ItemTracker(sources)
        val callback = callback()

        return LayoutFactory.vertical(2) {
            var maxLine = 0
            var lines = 0
            val body = LayoutFactory.vertical {
                val list = mutableListOf<AbstractWidget>()
                runCatching {
                    TreeFormatter.format(tree, tracker, WidgetBuilder(callback)) {
                        lines++
                        maxLine = maxOf(maxLine, it.width + 10)
                        list.add(it)
                    }
                }.onSuccess { list.forEach(::widget) }
                    .onFailure {
                        lines = 2
                        textDisplay {
                            append("An error occurred while displaying the recipe!")
                            this.color = TextColor.RED
                        }
                        textDisplay {
                            append("Error: ${it.message}")
                            this.color = TextColor.RED
                        }
                    }
            }.apply { visitChildren { child -> maxLine = maxOf(maxLine, child.width + 10) } }

            horizontal(5, MIDDLE) {
                val item = ExtraDisplays.inventoryBackground(1, 1, Displays.item(output.item, showTooltip = true).withPadding(2))
                display(item)
                vertical(alignment = MIDDLE) {
                    spacer(maxLine - item.getWidth() - 10)
                    display(Displays.component(output.itemName))
                    horizontal {
                        widget(
                            Displays.component(
                                Text.of {
                                    append("-")
                                    this.color = if (CraftHelperStorage.canModifyCount) TextColor.RED else TextColor.GRAY
                                },
                            ).asButtonLeft {
                                if (!CraftHelperStorage.canModifyCount) return@asButtonLeft

                                val value = CraftHelperStorage.selectedAmount
                                val newValue = if (Screen.hasShiftDown()) {
                                    value - 10
                                } else {
                                    value - 1
                                }
                                CraftHelperStorage.setAmount(max(1, newValue))
                                callback(true)
                            }.withTooltip(
                                Text.multiline(
                                    "§eClick§r to decrease by §c1",
                                    "§eShift + Click§r to decrease by §c10",
                                ).apply { this.color = TextColor.GRAY },
                            ).withoutTooltipDelay(),
                        )
                        textDisplay(" ${CraftHelperStorage.selectedAmount} ", shadow = true) {
                            this.color = TextColor.DARK_GRAY
                        }
                        widget(
                            Displays.component(
                                Text.of {
                                    append("+")
                                    this.color = if (CraftHelperStorage.canModifyCount) TextColor.GREEN else TextColor.GRAY
                                },
                            ).asButtonLeft {
                                if (!CraftHelperStorage.canModifyCount) return@asButtonLeft
                                val value = CraftHelperStorage.selectedAmount
                                val newValue = if (Screen.hasShiftDown()) {
                                    value + 10
                                } else {
                                    value + 1
                                }
                                CraftHelperStorage.setAmount(newValue)
                                callback(true)
                            }.withTooltip(
                                Text.multiline(
                                    "§eClick§r to increase by §a1",
                                    "§eShift + Click§r to increase by §a10",
                                ).apply { this.color = TextColor.GRAY },
                            ).withoutTooltipDelay(),
                        )
                    }
                }
                vertical(alignment = MIDDLE) {
                    widget(
                        Displays.component(Text.of(Icons.CROSS) { this.color = TextColor.RED }).asButtonLeft {
                            CraftHelperStorage.setSelected(null)
                            callback(false)
                        }.withoutTooltipDelay().withTooltip(Text.of("Close") { this.color = TextColor.RED }),
                    )
                    string("")
                }
            }

            body.let {
                widget(it.asScrollable(it.width + 10, McFont.height * 20.coerceAtMost(lines)))
            }
        }.asWidget().let {
            val background = BackgroundWidget(SkyOcean.minecraft("tooltip/background"), SkyOcean.minecraft("tooltip/frame"), widget = it, padding = 14)
            background.setPosition(10, (McScreen.self?.height?.div(2) ?: 0) - (it.height / 2))
            background
        }
    }
}
