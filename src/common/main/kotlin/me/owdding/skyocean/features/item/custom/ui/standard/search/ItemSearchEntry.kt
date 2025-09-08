package me.owdding.skyocean.features.item.custom.ui.standard.search

import me.owdding.skyocean.mixins.ModelManagerAccessor
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

object ItemSearchEntries {

    val ENTRIES by lazy {
        (McClient.self.modelManager as ModelManagerAccessor)
            .bakedItemModels()
            .keys
            .map { ItemModelSearchEntry(it) }
    }
}

interface ItemSearchEntry {

    val name: Component

    fun matches(query: String): Boolean {
        return this.name.stripped.contains(query, true)
    }

    fun resolve(parent: ItemStack): ItemStack
}

class ItemModelSearchEntry(
    val model: ResourceLocation
) : ItemSearchEntry {

    override val name: Component = if (this.model.namespace == "minecraft") {
        Text.of(this.model.path)
    } else {
        Text.of(this.model.toString())
    }

    override fun resolve(parent: ItemStack): ItemStack = parent.copy().apply {
        this[DataComponents.ITEM_MODEL] = model
    }
}

