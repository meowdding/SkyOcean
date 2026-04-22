package me.owdding.skyocean.utils.items

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

sealed interface ItemStackBlueprint {

    fun create(): ItemStack

    val item: Holder<Item>
    val count: Int
    val components: DataComponentPatch
    val isEmpty: Boolean

    @Suppress("DEPRECATION")
    companion object : MeowddingLogger by SkyOcean.featureLogger() {

        @IncludedCodec
        val MAP_CODEC: MapCodec<ItemStackBlueprint> = RecordCodecBuilder.mapCodec {
            it.group(
                Item.CODEC.fieldOf("id").forGetter(ItemStackBlueprint::item),
                ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(ItemStackBlueprint::count),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackBlueprint::components),
            ).apply(it, ItemStackBlueprint::of)
        }
        val CODEC: Codec<ItemStackBlueprint> = MAP_CODEC.codec()

        operator fun invoke(item: ItemStack) = of(item)
        operator fun invoke(item: Item) = of(item)
        operator fun invoke(holder: Holder<Item>, count: Int = 1, patch: DataComponentPatch = DataComponentPatch.EMPTY) = of(holder, count, patch)

        //~ if >= 26.1 'getItemHolder' -> 'typeHolder'
        fun of(item: ItemStack) = of(item.typeHolder(), item.count, item.componentsPatch)
        fun of(item: Item) = of(item.builtInRegistryHolder(), 1, DataComponentPatch.EMPTY)
        fun of(holder: Holder<Item>, count: Int = 1, patch: DataComponentPatch = DataComponentPatch.EMPTY): ItemStackBlueprint {
            if (holder.`is`(Items.AIR.builtInRegistryHolder())) {
                return EmptyStackBlueprint
            }
            return StackBlueprint(holder, count, patch)
        }
    }

    data class StackBlueprint(override val item: Holder<Item>, override val count: Int, override val components: DataComponentPatch) : ItemStackBlueprint {
        override fun create(): ItemStack = this.validate(ItemStack(this.item, this.count, this.components))

        private fun validate(result: ItemStack): ItemStack {
            val error = ItemStack.validateStrict(result).error()
            if (error.isPresent) {
                ItemStackBlueprint.warn("Can't create item stack with properties ${this}, error: ${error.get().message()}")
                return ItemStack.EMPTY
            } else {
                return result
            }
        }

        override val isEmpty: Boolean = false

    }

    data object EmptyStackBlueprint : ItemStackBlueprint {
        override fun create(): ItemStack = ItemStack.EMPTY

        @Suppress("DEPRECATION")
        override val item: Holder<Item> = Items.AIR.builtInRegistryHolder()
        override val count: Int = 1
        override val components: DataComponentPatch = DataComponentPatch.EMPTY
        override val isEmpty: Boolean = true
    }
}
