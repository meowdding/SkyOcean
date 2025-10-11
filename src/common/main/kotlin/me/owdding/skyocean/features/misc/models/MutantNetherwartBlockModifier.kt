package me.owdding.skyocean.features.misc.models

import me.owdding.skyocean.config.features.misc.ModelsConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

@ItemModifier
object MutantNetherwartBlockModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.misc.models.mutantNetherwartBlock"
    override val isEnabled: Boolean get() = ModelsConfig.mutantNetherwartBlock

    private val MUTANT_NETHER_WART = SkyBlockId.item("mutant_nether_stalk")

    override fun appliesTo(itemStack: ItemStack): Boolean = itemStack.getSkyBlockId() == MUTANT_NETHER_WART
    override fun itemOverride(itemStack: ItemStack): Item = Items.NETHER_WART_BLOCK
}
