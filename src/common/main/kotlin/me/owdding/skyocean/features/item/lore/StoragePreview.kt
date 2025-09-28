package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
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

@LoreModifier
object StoragePreview : AbstractLoreModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.storage_preview"
    override val isEnabled: Boolean get() = LoreModifierConfig.enableStoragePreview
    private val regex = Regex("Ender Chest Page .|Backpack Slot .{1,2}")

    override fun appliesTo(item: ItemStack): Boolean {
        val screen = McScreen.asMenu ?: return false
        if (!screen.title.stripped.contains("Storage")) return false

        return item.hoverName.stripped.matches(regex)
    }

    private var storageInstance: PlayerStorageInstance? = null

    override fun modify(item: ItemStack, list: MutableList<Component>): Boolean {
        val stripped = item.hoverName.stripped
        val page = stripped.filter { it.isDigit() }.toIntOrNull() ?: return false
        val enderChest = when {
            stripped.contains("ender chest", true) -> true
            stripped.contains("backpack", true) -> false
            else -> return false
        }

        val pages = if (enderChest) StorageAPI.enderchests else StorageAPI.backpacks
        this.storageInstance = pages.find { it.index == page - 1 }

        return this.storageInstance != null
    }

    override fun appendComponents(item: ItemStack, list: MutableList<ClientTooltipComponent>) {
        val storageInstance = storageInstance ?: return

        withComponentMerger(list) {
            addUntil { it.getWidth(McFont.self) <= McFont.self.width(" ") && it is ClientTextTooltip }
            read()
            add(InventoryTooltipComponent(storageInstance.items, 9))
        }
    }
}
