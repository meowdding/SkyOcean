package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.PlayerStorageInstance
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@ItemModifier
object StoragePreview : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.storage_preview"
    override val isEnabled: Boolean get() = LoreModifierConfig.enableStoragePreview
    private val regex = Regex("Ender Chest Page .|Backpack Slot .{1,2}")

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val screen = McScreen.asMenu ?: return false
        if (!screen.title.stripped.contains("Storage")) return false

        return itemStack.hoverName.stripped.matches(regex)
    }

    private var storageInstance: PlayerStorageInstance? = null

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result {
        val stripped = item.hoverName.stripped
        val page = stripped.filter { it.isDigit() }.toIntOrNull() ?: return Result.unmodified
        val enderChest = when {
            stripped.contains("ender chest", true) -> true
            stripped.contains("backpack", true) -> false
            else -> return Result.unmodified
        }

        val pages = if (enderChest) StorageAPI.enderchests else StorageAPI.backpacks
        this.storageInstance = pages.find { it.index == page - 1 }

        return Result.unmodified.takeUnless { this.storageInstance == null } ?: Result.unmodified
    }

    override fun appendComponents(item: ItemStack, list: MutableList<ClientTooltipComponent>): Result {
        val storageInstance = storageInstance ?: return Result.unmodified

        return withComponentMerger(list) {
            addUntil { it.getWidth(McFont.self) <= McFont.self.width(" ") && it is ClientTextTooltip }
            read()
            add(InventoryTooltipComponent(storageInstance.items, 9))
            Result.modified
        }
    }
}
