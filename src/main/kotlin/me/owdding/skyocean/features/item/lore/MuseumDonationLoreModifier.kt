package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.profile.items.museum.MuseumAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@ItemModifier
object MuseumDonationLoreModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.museum_donation"
    override val isEnabled: Boolean get() = LoreModifierConfig.museumDonation

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val id = itemStack.getSkyBlockId() ?: return false
        return MuseumAPI.isMuseumItem(id) && !MuseumAPI.isDonated(id)
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val rarity = item.getData(DataTypes.RARITY) ?: return@withMerger null
        if (!addUntilRarityLine(rarity)) return@withMerger null
        add("Not donated to Museum!") {
            this.color = TextColor.RED
            this.bold = true
        }
        Result.modified
    }
}
