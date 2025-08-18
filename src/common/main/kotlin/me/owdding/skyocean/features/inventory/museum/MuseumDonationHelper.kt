package me.owdding.skyocean.features.inventory.museum

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.CachedValue
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
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.copyFrom
import me.owdding.skyocean.utils.Utils.skyOceanPrefix
import me.owdding.skyocean.utils.Utils.wrap
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object MuseumDonationHelper : MeowddingLogger by SkyOcean.featureLogger(), RecipeView {

    val museumRegex = Regex(".*?[Mm]useum.*?")

    val itemCache = CachedValue { ItemTracker() }
    val itemTracker by itemCache

    @MustBeContainer
    @OnlyOnSkyBlock
    @Subscription
    fun inventoryChangeEvent(event: InventoryChangeEvent) {
        if (!event.title.matches(museumRegex)) return
        if (event.item !in Items.GRAY_DYE) return

        try {
            val data = MuseumRepoData.getDataByName(event.item.cleanName)
            when (data) {
                is MuseumArmour -> handleMuseumArmourData(event, data)
                is MuseumItem -> handleMuseumItemData(event, data)
            }
        } catch (error: MuseumRepoData.MuseumDataError) {
            error("${error.message}: ${error.type}")

            val item = when (error.type) {
                MuseumRepoData.MuseumDataError.Type.ITEM_NOT_FOUND -> return
                MuseumRepoData.MuseumDataError.Type.NO_MATCHING_MUSEUM_ITEM -> Items.BARRIER
                MuseumRepoData.MuseumDataError.Type.ARMOR_NOT_FOUND -> Items.RED_DYE
            }
            event.item.replaceVisually {
                copyFrom(event.item)
                skyOceanPrefix()
                this.item = item
                tooltip {
                    copyFrom(event.item)

                    if (!isEmpty()) space()

                    add("Can't find item with name ") {
                        append(event.item.hoverName.copy().wrap("'"))
                        this.color = TextColor.RED
                    }
                }
            }
        }
    }

    private fun handleMuseumItemData(event: InventoryChangeEvent, data: MuseumItem) {
        val id = data.skyoceanId
        val copy = itemTracker.snapshot()
        val items = copy.takeN(id, 1)
        val amount = items.sumOf { it.amount }
        event.item.replaceVisually {
            copyFrom(event.item)
            skyOceanPrefix()
            if (amount >= 1) {
                this.item = Items.GREEN_DYE
                return@replaceVisually
            }

            val recipe = SimpleRecipeApi.getBestRecipe(id)

            if (recipe == null) {
                debug("Recipe is null $id")
                this.item = Items.RED_DYE
                return@replaceVisually
            }
            val tree = ContextAwareRecipeTree(recipe, SkyOceanItemIngredient(id, 1), 1)
            val context = CraftHelperContext.create(tree, copy)
            create(context)
            val rootState = context.toState()
            if (rootState.childrenDone) {
                this.item = Items.YELLOW_DYE
            } else {
                this.item = Items.ORANGE_DYE
            }
        }
    }

    private fun handleMuseumArmourData(event: InventoryChangeEvent, data: MuseumArmour) {
        event.item.replaceVisually {
            copyFrom(event.item)
            skyOceanPrefix()
        }
    }

    @Subscription
    fun containerClose(event: ContainerCloseEvent) {
        itemCache.invalidate()
    }

    override fun create(
        state: CraftHelperState,
        widget: WidgetBuilder,
        widgetConsumer: (AbstractWidget) -> Unit,
    ) = Unit
}
