package me.owdding.skyocean.compat

import me.owdding.skyocean.SkyOcean
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.BiConsumer

object CatharsisSupport {

    private var idConsumer: BiConsumer<ItemStack, Identifier> = BiConsumer { _, _ -> }
    private var disabledConsumer: BiConsumer<ItemStack, Boolean> = BiConsumer { _, _ -> }

    @JvmStatic
    fun id(consumer: BiConsumer<ItemStack, Identifier>) {
        this.idConsumer = consumer
    }

    @JvmStatic
    fun disabled(consumer: BiConsumer<ItemStack, Boolean>) {
        this.disabledConsumer = consumer
    }

    fun ItemStack.disableCatharsisModifications() {
        disabledConsumer.accept(this, true)
    }

    fun ItemStack.withCatharsisId(path: String): ItemStack = apply {
        idConsumer.accept(this, SkyOcean.id(path))
    }

    fun Item.withCatharsisId(path: String): ItemStack = defaultInstance.apply {
        idConsumer.accept(this, SkyOcean.id(path))
    }
}
