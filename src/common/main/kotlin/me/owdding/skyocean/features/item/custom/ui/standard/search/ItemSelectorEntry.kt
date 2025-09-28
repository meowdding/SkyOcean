package me.owdding.skyocean.features.item.custom.ui.standard.search

import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.mixins.ModelManagerAccessor
import me.owdding.skyocean.utils.Utils.applyCatching
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.set
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.api.remote.api.SimpleItemAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.compoundTag
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.*

object ItemSearchEntries {

    val ENTRIES by lazy {
        listOf(
            (McClient.self.modelManager as ModelManagerAccessor)
                .bakedItemModels()
                .keys
                .map { ItemModelSearchEntry(it) },
            SimpleItemAPI.getAllIds().map { SkyBlockModelEntry(it) },
        ).flatten().sortedBy { it.name.stripped }
    }
}

interface ModelSearchEntry {

    val name: Component

    fun matches(query: String): Boolean {
        return this.name.stripped.contains(query, true)
    }

    fun CustomItemData.applyToData()

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

    override fun CustomItemData.applyToData() {
        this[CustomItemDataComponents.MODEL] = StaticModel(model)
        this[CustomItemDataComponents.SKIN] = null
    }

    override fun resolve(parent: ItemStack): ItemStack = itemBuilder(parent) {
        this[DataComponents.ITEM_MODEL] = model
        set(DataComponents.CUSTOM_DATA, null)
    }
}

data class SkyBlockModelEntry(
    val model: SkyBlockId,
) : ModelSearchEntry {
    val animatedSkin = runCatching { AnimatedSkyblockSkin(model) }.getOrNull()
    val normalSkin = runCatching { SkyblockSkin(model) }.getOrNull()
    val skin = animatedSkin ?: normalSkin
    override val name: Component = model.toItem().hoverName

    override fun CustomItemData.applyToData() {
        this[CustomItemDataComponents.MODEL] = SkyblockModel(model)
        this[CustomItemDataComponents.SKIN] = skin
    }

    val uuidString = UUID.randomUUID().toString()

    override fun resolve(parent: ItemStack): ItemStack = itemBuilder(parent) {
        val item = model.toItem()
        set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item.getItemModel()))
        set(
            DataComponents.CUSTOM_DATA,
            CustomData.of(
                compoundTag {
                    putString("skyocean:static_item", uuidString)
                },
            ),
        )
    }.applyCatching {
        this.getKey()?.let {
            CustomItems.staticMap[it] = CustomItemData(it).apply {
                this[CustomItemDataComponents.SKIN] = skin
            }
        }
    }

}

