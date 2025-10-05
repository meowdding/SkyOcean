package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage.setAmount
import me.owdding.skyocean.data.profile.CraftHelperStorage.setSelected
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
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
    abstract fun applies(event: InventoryChangeEvent): SkyOceanItemIngredient?

    fun tryModify(event: InventoryChangeEvent) {
        applies(event)?.let { modify(event, it) }
    }

    private fun modify(event: InventoryChangeEvent, ingredient: SkyOceanItemIngredient) {
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
            }

            onClick {
                setSelected(ingredient.id)
                setAmount(ingredient.amount)
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
        if (!CraftHelperConfig.enabled) return
        modifiers.forEach { it.tryModify(this) }
    }
}

@AutoCollect("CraftHelperModifiers")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CraftHelperModifier
