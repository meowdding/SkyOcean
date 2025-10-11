package me.owdding.skyocean.features.misc.models

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.ModelsConfig
import me.owdding.skyocean.events.BlockModelEvent
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

@Module
@ItemModifier
object CarvedPumpkinModifier : AbstractItemModifier() {

    override val displayName: Component = +"skyocean.config.misc.models.noCarvedPumpkins"
    override val isEnabled get() = ModelsConfig.noCarvedPumpkins

    private val allowedIds: Set<SkyBlockId> = setOf("enchanted_pumpkin", "pumpkin").mapTo(mutableSetOf(), SkyBlockId::item)

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val id = itemStack.getSkyBlockId() ?: return false
        return id in allowedIds
    }

    override fun itemOverride(itemStack: ItemStack): Item = Items.PUMPKIN


    @Subscription
    @OnlyIn(GARDEN, THE_BARN)
    fun onBlockModel(event: BlockModelEvent) {
        if (!isEnabled) return
        if (event.block != Blocks.CARVED_PUMPKIN) return
        event.block = Blocks.PUMPKIN
    }

}
