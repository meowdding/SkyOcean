package me.owdding.skyocean.features.inventory.museum

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.item.lore.AbstractLoreModifier
import me.owdding.skyocean.features.item.lore.InventoryTooltipComponent
import me.owdding.skyocean.features.item.lore.LoreModifier
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperContext
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.repo.museum.MuseumArmour
import me.owdding.skyocean.repo.museum.MuseumItem
import me.owdding.skyocean.repo.museum.MuseumRepoData
import me.owdding.skyocean.repo.museum.MuseumRepoData.MuseumDataError.Type.ARMOR_NOT_FOUND
import me.owdding.skyocean.repo.museum.MuseumRepoData.MuseumDataError.Type.ITEM_NOT_FOUND
import me.owdding.skyocean.repo.museum.MuseumRepoData.MuseumDataError.Type.NO_MATCHING_MUSEUM_ITEM
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.addAll
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.modifyTooltip
import me.owdding.skyocean.utils.Utils.rebuild
import me.owdding.skyocean.utils.Utils.skipRemaining
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.Utils.wrap
import me.owdding.skyocean.utils.chat.Icons
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
@LoreModifier
object MuseumDonationHelper : RecipeView, AbstractLoreModifier() {

    private val logger: MeowddingLogger = SkyOcean.featureLogger()
    private val modifierCache: MutableMap<String, Pair<ComponentModifier?, TooltipComponentModifier?>> = mutableMapOf()

    val museumRegex = Regex(".*[Mm]useum.*")

    private val itemCache = CachedValue { ItemTracker() }
    private val itemTracker by itemCache

    override val displayName: Component? = null
    override val extraNames: List<Component>
        get() = buildList {
            if (MiscConfig.itemSearchMuseumIntegration) {
                add(+"skyocean.config.misc.itemSearch.museumIntegration")
            }
            if (MiscConfig.museumArmourPieces) {
                add(+"skyocean.config.misc.museumArmourPieces")
            }
        }
    override val isEnabled: Boolean = true

    private context(item: ItemStack) fun registerModifier(component: ComponentModifier) {
        modifierCache[item.cleanName] = component to null
    }

    private context(item: ItemStack) fun buildModifiers(init: ModifierBuilder.() -> Unit) {
        val builder = object : ModifierBuilder {
            override var component: ComponentModifier? = null
            override var tooltip: TooltipComponentModifier? = null

            override fun registerModifier(component: ComponentModifier) {
                this.component = component
            }

            override fun registerComponentModifier(modifier: TooltipComponentModifier) {
                this.tooltip = modifier
            }
        }
        builder.init()
        if (builder.component == null && builder.tooltip == null) return
        modifierCache[item.cleanName] = builder.component to builder.tooltip
    }

    @MustBeContainer
    @OnlyOnSkyBlock
    @Subscription
    fun inventoryChangeEvent(event: InventoryChangeEvent) {
        if (!museumRegex.matches(event.title)) return
        if (event.item !in Items.GRAY_DYE) return
        if (!MiscConfig.museumArmourPieces && !MiscConfig.itemSearchMuseumIntegration) return

        try {
            when (val data = MuseumRepoData.getDataByName(event.item.cleanName)) {
                is MuseumArmour -> data.handleMuseumArmourData(event)
                is MuseumItem if MiscConfig.itemSearchMuseumIntegration -> data.handleMuseumItemData(event)
            }
        } catch (error: MuseumRepoData.MuseumDataError) {
            logger.error("${error.message}: ${error.type}")

            val item: Item = when (error.type) {
                ITEM_NOT_FOUND -> Items.BLACKSTONE
                NO_MATCHING_MUSEUM_ITEM -> Items.BARRIER
                ARMOR_NOT_FOUND -> Items.RED_DYE
            }

            event.item.skyoceanReplace {
                this.item = item

                modifyTooltip {
                    space()
                    add("Can't find item with name ") {
                        append(event.item.hoverName.copy().wrap("'"))
                        this.color = TextColor.RED
                    }
                }
            }
        }
    }

    private fun MuseumItem.handleMuseumItemData(event: InventoryChangeEvent) {
        val data = this
        val id = data.skyblockId
        val copy = itemTracker.snapshot()
        val items = copy.takeN(id, 1)
        val amount = items.sumOf { it.amount }
        event.item.skyoceanReplace(false) {
            if (amount >= 1) {
                this.item = Items.GREEN_DYE
                registerModifier {
                    beforeWiki()
                    add("This item was found on your profile!") { this.color = TextColor.GREEN }
                    space()
                    addAll(items.first().context.collectLines())
                    skipRemaining()
                }
                onClick {
                    val item = items.first()
                    ItemHighlighter.setHighlight(ReferenceItemFilter.create(item.context, item.itemStack))
                    item.context.open()
                }
                return@skyoceanReplace
            }

            val rootState = copy.toState(id)

            if (rootState == null) {
                logger.debug("Recipe is null $id")
                this.item = Items.RED_DYE

                registerModifier {
                    beforeWiki()
                    add("No recipe found for item!") { this.color = TextColor.RED }
                    space()
                }
                return@skyoceanReplace
            }

            if (rootState.childrenDone) {
                this.item = Items.YELLOW_DYE
                registerModifier {
                    beforeWiki()
                    add("You have all materials to craft this item!") { this.color = TextColor.GREEN }
                    add("Click to set as craft helper item!") { this.color = TextColor.GREEN }
                    skipRemaining()
                }
            } else {
                this.item = Items.ORANGE_DYE
                registerModifier {
                    beforeWiki()
                    add("This item can be crafted!") { this.color = TextColor.GRAY }
                    add("Click to set as craft helper item!") { this.color = TextColor.YELLOW }
                    skipRemaining()
                }
            }

            onClick {
                CraftHelperStorage.setSelected(id)
                CraftHelperStorage.setAmount(1)
                CraftHelperStorage.save()
                McScreen.self.rebuild()
            }
        }
    }

    fun ItemTracker.toState(id: SkyBlockId): CraftHelperState? {
        val recipe = SimpleRecipeApi.getBestRecipe(id) ?: return null
        val tree = ContextAwareRecipeTree(recipe, SkyOceanItemIngredient(id, 1), 1)
        val context = CraftHelperContext.create(tree, this)
        evaluateNode(context)
        return context.toState()
    }

    private fun ListMerger<Component>.beforeWiki() = this.addUntil(::isWikiLine)
    private fun isWikiLine(component: Component) = component.stripped.contains("Click to view on the")

    private fun MuseumArmour.handleMuseumArmourData(event: InventoryChangeEvent) = context(event.item) {
        val data = this
        val items = data.armorIds.map { SkyBlockId.item(it) }
        val copy = itemTracker.snapshot()

        val itemList = items.map { it to it.toItem() }.sortedBy { (_, item) -> item.getPriority() }
        buildModifiers {
            if (MiscConfig.itemSearchMuseumIntegration) registerModifier {
                beforeWiki()
                val copy = copy.snapshot()
                itemList.forEach { (id, stack) ->
                    val take = copy.takeN(id, 1)
                    if (take.sumOf { it.amount } >= 1) {
                        add {
                            append(Icons.CHECKMARK) { this.color = TextColor.GREEN }
                            append(" ")
                            append(stack.hoverName)
                        }
                        return@forEach
                    }

                    val state = copy.toState(id)
                    add {
                        if (state == null || !state.childrenDone) {
                            append(Icons.CROSS) { this.color = TextColor.RED }
                        } else {
                            append(Icons.WARNING) { this.color = TextColor.YELLOW }
                        }
                        append(" ")
                        append(stack.hoverName)

                    }
                }
                space()
            }
            if (MiscConfig.museumArmourPieces) registerComponentModifier {
                addUntil { it.getWidth(McFont.self) <= McFont.self.width(" ") && it is ClientTextTooltip }
                read()
                add(
                    InventoryTooltipComponent(
                        itemList.map { it.second },
                        4, true,
                    ),
                )
            }
        }
    }

    private fun ItemStack.getPriority() = when (this[DataTypes.CATEGORY]?.name?.lowercase()) {
        "helmet" -> 1
        "chestplate" -> 2
        "leggings" -> 3
        "boots" -> 4
        "necklace" -> 5
        "cloak" -> 6
        "belt" -> 7
        "bracelet", "gloves" -> 8
        else -> {
            logger.info("Unknown category ${this[DataTypes.CATEGORY]?.name?.lowercase()}")
            Int.MAX_VALUE
        }
    }

    @Subscription(ContainerCloseEvent::class)
    fun containerClose() {
        itemCache.invalidate()
        modifierCache.clear()
    }

    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = Unit

    override fun appliesTo(item: ItemStack): Boolean = itemCache.hasValue()

    override fun modify(item: ItemStack, list: MutableList<Component>): Boolean {
        if (item.cleanName !in modifierCache) return false
        return withMerger(list) {
            modifierCache[item.cleanName]?.first?.publicMerge(this)
            true
        }
    }

    override fun appendComponents(
        item: ItemStack,
        list: MutableList<ClientTooltipComponent>,
    ) {
        if (item.cleanName !in modifierCache) return
        return withComponentMerger(list) {
            modifierCache[item.cleanName]?.second?.publicMerge(this)
        }
    }
}

private fun interface ComponentModifier {
    fun publicMerge(merger: ListMerger<Component>) = merger.merge()
    fun ListMerger<Component>.merge()
}

private fun interface TooltipComponentModifier {
    fun publicMerge(merger: ListMerger<ClientTooltipComponent>) = merger.merge()
    fun ListMerger<ClientTooltipComponent>.merge()
}

private interface ModifierBuilder {
    var component: ComponentModifier?
    var tooltip: TooltipComponentModifier?

    fun registerModifier(component: ComponentModifier)
    fun registerComponentModifier(modifier: TooltipComponentModifier)
}
