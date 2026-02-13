package me.owdding.skyocean.features.misc

import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData

@ItemModifier
object WateringCanDurability : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.misc.watering_can_durability"
    override val isEnabled: Boolean get() = MiscConfig.enableWateringCanDurability

    override fun appliesTo(itemStack: ItemStack) = itemStack.getData(DataTypes.WATER_LEVEL) != null

    override fun getExtraComponents(itemStack: ItemStack): DataComponentPatch? {
        val (currentWater, maxWater) = itemStack.getData(DataTypes.WATER_LEVEL) ?: return null

        return DataComponentPatch.builder()
            .set(DataComponents.MAX_DAMAGE, maxWater)
            .set(DataComponents.DAMAGE, (maxWater - currentWater).coerceAtLeast(0))
            .build()
    }
}
