package me.owdding.skyocean.api

import me.owdding.skyocean.utils.Utils.ItemBuilder
import me.owdding.skyocean.utils.Utils.compoundTag
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.putCompound
import me.owdding.skyocean.utils.Utils.set
import me.owdding.skyocean.utils.Utils.toData
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemUtils
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic

object AttributeApi {

    private val cache: MutableMap<String, ItemStack?> = mutableMapOf()

    fun getAttributeByIdOrNull(id: String) = cache.getOrPut(id) {
        val attribute = RepoAPI.attributes().getAttribute(id)
        if (attribute == null) return@getOrPut null

        val item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(attribute.item.lowercase()))
            .takeUnless { it == Items.AIR }
            ?: Items.BARRIER

        ItemBuilder(item) {
            if (attribute.texture != null) {
                copyFrom(ItemUtils.createSkull(attribute.texture!!))
            }
            this[DataComponents.ITEM_NAME] = !attribute.shardName
            this[DataComponents.CUSTOM_NAME] = Text.of(attribute.shardName) {
                this.italic = false
                runCatching {
                    this.color = SkyBlockRarity.valueOf(attribute.rarity).color
                }
            }

            val rawLore = attribute.lore
            val lore = rawLore.map { !it }.toMutableList()
                .also { it.addFirst(Text.of(attribute.name) { this.color = TextColor.GOLD }) }.toList()

            this[DataComponents.LORE] = ItemLore(lore, lore)
            this[DataComponents.CUSTOM_DATA] = compoundTag {
                putString("id", "ATTRIBUTE_SHARD")
                putCompound("attributes") {
                    putInt(attribute.id, 1)
                }
            }.toData()
        }
    }

}
