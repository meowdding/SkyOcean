package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
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

@LoreModifier
object MuseumDonationLoreModifier : AbstractLoreModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.museum_donation"
    override val isEnabled: Boolean get() = LoreModifierConfig.museumDonation

    override fun appliesTo(item: ItemStack): Boolean {
        val id = item.getSkyBlockId() ?: return false
        return MuseumAPI.isMuseumItem(id) && !MuseumAPI.isDonated(id)
    }

    override fun modify(item: ItemStack, list: MutableList<Component>) = withMerger(list) {
        val rarity = item.getData(DataTypes.RARITY) ?: return@withMerger false
        if (!addUntilRarityLine(rarity)) return@withMerger false
        add("Not donated to Museum!") {
            this.color = TextColor.RED
            this.bold = true
        }
    }


}
