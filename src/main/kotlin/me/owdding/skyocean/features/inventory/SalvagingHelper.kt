package me.owdding.skyocean.features.inventory

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.inventory.InventoryConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.*

@Module
@ItemModifier
object SalvagingHelper : AbstractItemModifier() {

    private const val RED = 0xffd20f39.toInt()
    private const val SALVAGE_MENU_TITLE = "Salvage Items"

    override val displayName: Component = +"skyocean.config.inventory.salvaging_helper"
    override val isEnabled: Boolean get() = InventoryConfig.salvagingHelper

    override val modifierSources: List<ModifierSource> = listOf(ModifierSource.INVENTORY)
    private val items: WeakHashMap<ItemStack, Unit> = WeakHashMap()

    @Subscription(ContainerCloseEvent::class)
    fun onInventoryClose() {
        if (!InventoryConfig.salvagingHelper) return
        items.clear()
    }

    override fun clickAction(itemStack: ItemStack): ((Int) -> Unit?)? {
        if (itemStack.getSkyBlockId() != null) return null
        if (items.isEmpty()) return null
        items.clear()

        if (!InventoryConfig.salvagingHelperBlockSalvage) return null
        return {
            McPlayer.self?.playSound(SoundEvents.ANVIL_LAND, 0.8f, 1f)
            Unit //non-null return value cancels the interaction
        }
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result {
        if (item.getSkyBlockId() != null) return Result.unmodified

        return withMerger(list) {
            addRemaining()
            space()
            add(Text.of("Blocked you from salvaging as one or more", TextColor.RED))
            add(Text.of("of the items haven't been donated to the museum!", TextColor.RED))

            Result.modified
        }
    }

    override fun backgroundColor(itemStack: ItemStack): Int = RED

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val menu = McScreen.asMenu ?: return false
        val sbId = itemStack.getSkyBlockId()
        return when {
            menu.title.stripped != SALVAGE_MENU_TITLE -> false
            itemStack.cleanName == SALVAGE_MENU_TITLE -> true
            sbId == null -> false
            InventoryConfig.salvagingHelperHighlight && MuseumAPI.isMuseumItem(sbId) && !MuseumAPI.isDonated(sbId) -> {
                items[itemStack] = Unit
                true
            }

            else -> true
        }

    }

}
