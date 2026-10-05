package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.skyocean.compat.CatharsisSupport.disableCatharsisModifications
import me.owdding.skyocean.compat.CatharsisSupport.withCatharsisId
import me.owdding.skyocean.config.features.misc.crafthelper.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.data.profile.CraftHelperStorage.addToIngredientRecipe
import me.owdding.skyocean.data.profile.CraftHelperStorage.set
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperManager
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.IngredientCraftHelperRecipe
import me.owdding.skyocean.generated.SkyOceanCraftHelperModifiers
import me.owdding.skyocean.utils.Utils.refreshScreen
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

abstract class AbstractCraftHelperModifier {
    abstract fun applies(event: InventoryChangeEvent): CraftHelperRecipe?

    fun tryModify(event: InventoryChangeEvent) {
        applies(event)?.let { modify(event, it) }
    }

    private fun modify(event: InventoryChangeEvent, ingredient: CraftHelperRecipe) {
        event.item.disableCatharsisModifications().withCatharsisId("crafthelper")
        event.item.skyoceanReplace {
            this.item = Items.DIAMOND_PICKAXE
            set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true))
            name(
                Text.of("Craft Helper") {
                    this.color = TextColor.GREEN
                },
            )
            tooltip {
                add("Set as selected craft helper item!") {
                    this.color = TextColor.GRAY
                }
                if (ingredient is CraftHelperRecipe.Ingredients) {
                    add("Hold Shift to add it to your current recipe!") {
                        this.color = TextColor.GRAY
                    }
                }
            }

            onClick {
                if (McScreen.isShiftDown && ingredient is CraftHelperRecipe.Ingredients) {
                    addToIngredientRecipe(ingredient)
                } else {
                    if (CraftHelperStorage.data is IngredientCraftHelperRecipe) {
                        return@onClick event.item.skyoceanReplace {
                            this.item = Items.COPPER_PICKAXE
                            set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true))
                            name(
                                Text.of("Override Custom Recipe?") {
                                    this.color = TextColor.GRAY
                                },
                            )
                            tooltip {
                                add("Click again to override custom recipe!") {
                                    this.color = TextColor.RED
                                }
                            }
                            onClick {
                                set(ingredient)
                                McScreen.refreshScreen()
                            }
                        }
                    }
                    set(ingredient)
                }
                McScreen.refreshScreen()
            }
        }
    }
}

@Module
object CraftHelperModifiers {
    val modifiers: List<AbstractCraftHelperModifier> = SkyOceanCraftHelperModifiers.collected.toList()

    @Subscription
    private fun InventoryChangeEvent.onInventory() {
        if (!CraftHelperConfig.enabled || !CraftHelperConfig.quickSet) return
        modifiers.forEach { it.tryModify(this) }
    }
}

@AutoCollect("CraftHelperModifiers")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CraftHelperModifier
