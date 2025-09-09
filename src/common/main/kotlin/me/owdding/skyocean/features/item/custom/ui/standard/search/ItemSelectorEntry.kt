package me.owdding.skyocean.features.item.custom.ui.standard.search

import me.owdding.skyocean.api.SimpleItemApi
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.mixins.ModelManagerAccessor
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.applyCatching
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.compoundTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

object ItemSearchEntries {

    val ENTRIES by lazy {
        listOf(
            (McClient.self.modelManager as ModelManagerAccessor)
                .bakedItemModels()
                .keys
                .map { ItemModelSearchEntry(it) },
            SimpleItemApi.getAllIds().map { SkyBlockModelEntry(it) },
        ).flatten()
    }
}

interface ModelSearchEntry {

    val name: Component

    fun matches(query: String): Boolean {
        return this.name.stripped.contains(query, true)
    }
    fun toItemDataComponent(): ItemModel

    fun resolve(parent: ItemStack): ItemStack
}

data class ItemModelSearchEntry(
    val model: ResourceLocation,
) : ModelSearchEntry {

    override val name: Component = if (this.model.namespace == ResourceLocation.DEFAULT_NAMESPACE) {
        Component.translatableWithFallback(this.model.toLanguageKey(), this.model.path)
    } else {
        Text.of(this.model.toString())
    }

    override fun toItemDataComponent(): ItemModel = StaticModel(model)

    override fun resolve(parent: ItemStack): ItemStack = parent.copy().apply {
        this[DataComponents.ITEM_MODEL] = model
    }
}

data class SkyBlockModelEntry(
    val model: SkyOceanItemId,
) : ModelSearchEntry {
    override val name: Component = model.toItem().hoverName
    override fun toItemDataComponent(): ItemModel = SkyblockModel(model)

    override fun resolve(parent: ItemStack): ItemStack = Utils.itemBuilder(parent.item) {
        val item = model.toItem()
        copyFrom(parent)
        set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item.getItemModel()))
        set(DataComponents.PROFILE, item[DataComponents.PROFILE])
        set(
            DataComponents.CUSTOM_DATA,
            CustomData.of(
                compoundTag {
                    putBoolean("skyocean:static_item", true)
                },
            ),
        )
    }.applyCatching {
        this.getKey()?.let {
            CustomItems.staticMap[it] = CustomItemData(it).apply {
                this[CustomItemDataComponents.SKIN] = AnimatedSkyblockSkin(model)
            }
        }
    }

}

