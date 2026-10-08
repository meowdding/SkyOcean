package me.owdding.skyocean.features.inventory.museum

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.compat.CatharsisSupport.disableCatharsisModifications
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.item.lore.InventoryTooltipComponent
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.features.item.search.highlight.ItemHighlighter
import me.owdding.skyocean.features.item.search.search.ReferenceItemFilter
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.IngredientCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperContext
import me.owdding.skyocean.features.recipe.crafthelper.views.CraftHelperState
import me.owdding.skyocean.features.recipe.crafthelper.views.RecipeView
import me.owdding.skyocean.features.recipe.crafthelper.views.WidgetBuilder
import me.owdding.skyocean.repo.museum.MuseumArmour
import me.owdding.skyocean.repo.museum.MuseumItem
import me.owdding.skyocean.repo.museum.MuseumRepoData
import me.owdding.skyocean.repo.museum.MuseumRepoData.MuseumDataError.Type.*
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.addAll
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.modifyTooltip
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.refreshScreen
import me.owdding.skyocean.utils.Utils.skipRemaining
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.Utils.wrap
import me.owdding.skyocean.utils.chat.Icons
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
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
import tech.thatgravyboat.skyblockapi.impl.ColoredItems
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.*

@Module
@ItemModifier
object MuseumDonationHelper : RecipeView, AbstractItemModifier() {
    data class State(
        var item: Item? = null,
        var onClick: ((Int) -> Unit)? = null,
        var componentModifier: ((item: ItemStack, list: MutableList<Component>, previousResult: Result?) -> Result) = { _, _, _ -> Result.unmodified },
        var clientComponentModifier: ((item: ItemStack, list: MutableList<ClientTooltipComponent>) -> Result) = { _, _ -> Result.unmodified },
    )

    private val modifierCache: MutableMap<ItemStack, State> = WeakHashMap()


    private val logger: MeowddingLogger = SkyOcean.featureLogger()

    private val group = RemoteStrings.resolve()
    val museumRegex by group.regex(".*[Mm]useum.*")

    private val itemCache = CachedValue { ItemTracker() }
    private val itemTracker by itemCache

    override val displayName: Component get() = !""
    override val displayNames: List<Component>
        get() = buildList {
            if (MiscConfig.itemSearchMuseumIntegration) {
                add(+"skyocean.config.misc.itemSearch.museumIntegration")
            }
            if (MiscConfig.museumArmourPieces) {
                add(+"skyocean.config.misc.museumArmourPieces")
            }
        }
    override val isEnabled: Boolean = true

    @MustBeContainer
    @OnlyOnSkyBlock
    @Subscription
    fun inventoryChangeEvent(event: InventoryChangeEvent) {
        if (!museumRegex.matches(event.title)) return
        if (event.item !in Items.DYE.gray()) return
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
                ARMOR_NOT_FOUND -> Items.DYE.red()
            }

            event.item.disableCatharsisModifications()
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

    private fun ItemStack.deferModifications(init: State.() -> Unit) {
        modifierCache[this] = State().apply(init)
    }

    private fun MuseumItem.handleMuseumItemData(event: InventoryChangeEvent) {
        val data = this
        val id = data.skyblockId
        val copy = itemTracker.snapshot()
        val items = copy.takeN(id, 1)
        val amount = items.sumOf { it.amount }
        event.item.deferModifications {
            if (amount >= 1) {
                this.item = Items.DYE.green()
                componentModifier = { _, list, _ ->
                    withMerger(list) {
                        
                        add("This item was found on your profile!") { this.color = TextColor.GREEN }
                        space()
                        addAll(items.first().context.collectLines())
                        skipRemaining()
                        Result.modified
                    }
                }
                onClick = {
                    val item = items.first()
                    ItemHighlighter.setHighlight(ReferenceItemFilter.create(item.context, item.itemStack))
                    item.context.open()
                }
                return@deferModifications
            }

            val rootState = copy.toState(id)

            if (rootState == null) {
                logger.debug("Recipe is null $id")
                this.item = Items.DYE.red()

                componentModifier = { _, list, _ ->
                    withMerger(list) {
                        
                        add("No recipe found for item!") { this.color = TextColor.RED }
                        space()
                        Result.modified
                    }
                }
                return@deferModifications
            }

            if (rootState.childrenDone) {
                this.item = Items.DYE.yellow()
                componentModifier = { _, list, _ ->
                    withMerger(list) {
                        
                        add("You have all materials to craft this item!") { this.color = TextColor.GREEN }
                        add("Click to set as craft helper item!") { this.color = TextColor.GREEN }
                        skipRemaining()
                        Result.modified
                    }
                }
            } else {
                this.item = Items.DYE.orange()
                componentModifier = { _, list, _ ->
                    withMerger(list) {
                        
                        add("This item can be crafted!") { this.color = TextColor.GRAY }
                        add("Click to set as craft helper item!") { this.color = TextColor.YELLOW }
                        skipRemaining()
                        Result.modified
                    }
                }
            }

            onClick = {
                CraftHelperStorage.setSelected(id)
                CraftHelperStorage.setAmount(1)
                CraftHelperStorage.save()
                McScreen.refreshScreen()
            }
        }
    }

    fun ItemTracker.toState(id: SkyBlockId): CraftHelperState? {
        val recipe = SimpleRecipeApi.getBestRecipe(id) ?: return null
        val tree = CraftHelperTree(recipe, SkyOceanItemIngredient(id, 1), 1)
        val context = CraftHelperContext.create(tree, this)
        evaluateNode(context)
        return context.toState()
    }

    private fun MuseumArmour.handleMuseumArmourData(event: InventoryChangeEvent) = context(event.item) {
        val data = this
        val items = data.armorIds.map { SkyBlockId.item(it) }
        val copy = itemTracker.snapshot()

        val itemList = items.map { it to it.toItem() }.sortedBy { (_, item) -> item.getPriority() }
        event.item.deferModifications {
            var canBeCrafted = true
            var hasAllItems = true
            val extra: MutableList<MutableComponent.() -> Unit> = mutableListOf()
            val copy = copy.snapshot()
            itemList.forEach { (id, stack) ->
                val take = copy.takeN(id, 1)
                if (take.sumOf { it.amount } >= 1) {
                    extra.add {
                        append(Icons.CHECKMARK) { this.color = TextColor.GREEN }
                        append(" ")
                        append(stack.hoverName)
                    }
                    return@forEach
                }
                hasAllItems = false

                val state = copy.toState(id)
                extra.add {
                    if (state == null || !state.childrenDone) {
                        append(Icons.CROSS) { this.color = TextColor.RED }
                        canBeCrafted = false
                    } else {
                        append(Icons.WARNING) { this.color = TextColor.YELLOW }
                    }
                    append(" ")
                    append(stack.hoverName)

                }
            }
            if (MiscConfig.itemSearchMuseumIntegration) componentModifier = { _, list, _ ->
                withMerger(list) {
                    
                    extra.forEach { add(it) }
                    space()
                    Result.modified
                }
            }
            if (MiscConfig.museumArmourPieces) clientComponentModifier = { item, list ->
                withComponentMerger(list) {
                    addUntil { it.getWidth(McFont.self) <= McFont.self.width(" ") && it is ClientTextTooltip }
                    read()
                    add(
                        InventoryTooltipComponent(
                            itemList.map { it.second },
                            4, true,
                        ),
                    )
                    Result.modified
                }
            }
            if (MiscConfig.itemSearchMuseumIntegration) {
                if (hasAllItems) {
                    //? >= 26.3
                    item = Items.CUSHION.green
                    //? < 26.3
                    //item = Items.GREEN_DYE
                } else if (canBeCrafted) {
                    //? >= 26.3
                    item = Items.CUSHION.yellow
                    //? < 26.3
                    //item = Items.YELLOW_DYE
                } else {
                    //? >= 26.3
                    item = Items.CUSHION.orange
                    //? < 26.3
                    //item = Items.ORANGE_DYE
                }

                val previous = componentModifier

                componentModifier = { item, list, result ->
                    previous.invoke(item, list, result)
                    list.add(CommonComponents.EMPTY)
                    list.add(Text.of("Click to set as craft helper items!") { this.color = TextColor.GREEN })
                    Result.modified
                }

                onClick = {
                    CraftHelperStorage.set(
                        IngredientCraftHelperRecipe(
                            itemList.map { SkyOceanItemIngredient(it.first) }.toMutableList()
                        )
                    )
                    McScreen.refreshScreen()
                }
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

    override fun appliesTo(itemStack: ItemStack): Boolean = itemCache.hasValue()

    override fun itemOverride(itemStack: ItemStack): Item? = modifierCache[itemStack]?.item
    override fun clickAction(itemStack: ItemStack): ((Int) -> Unit?)? = modifierCache[itemStack]?.onClick
    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result =
        modifierCache[item]?.componentModifier?.invoke(item, list, previousResult) ?: Result.unmodified

    override fun appendComponents(item: ItemStack, list: MutableList<ClientTooltipComponent>): Result =
        modifierCache[item]?.clientComponentModifier?.invoke(item, list) ?: Result.unmodified

    override fun modified(itemStack: ItemStack, visualItem: ItemStack?) {
        modifierCache[visualItem ?: return] = modifierCache[itemStack] ?: return
    }
}
