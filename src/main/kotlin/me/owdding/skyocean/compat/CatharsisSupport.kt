package me.owdding.skyocean.compat

import me.owdding.lib.utils.KnownMods
import me.owdding.skyocean.ApiDebug
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.debug.DebugBuilder
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.BiConsumer
import java.util.function.Predicate

object CatharsisSupport {

    private var idConsumer: BiConsumer<ItemStack, Identifier> = BiConsumer { _, _ -> }
    private var hasIdConsumer = false
    private var disabledConsumer: BiConsumer<ItemStack, Boolean> = BiConsumer { _, _ -> }
    private var hasDisabledConsumer = false
    private var hiddenModElementsProvider: Predicate<String> = Predicate { false }
    private var hasHiddenModElementsProvider = false

    @JvmStatic
    fun id(consumer: BiConsumer<ItemStack, Identifier>) {
        this.idConsumer = consumer
    }

    @JvmStatic
    fun disabled(consumer: BiConsumer<ItemStack, Boolean>) {
        this.disabledConsumer = consumer
    }

    @JvmStatic
    fun hiddenModElements(provider: Predicate<String>) {
        this.hiddenModElementsProvider = provider
        this.hasHiddenModElementsProvider = true
    }

    fun ItemStack.disableCatharsisModifications() = apply {
        disabledConsumer.accept(this, true)
    }

    fun ItemStack.withCatharsisId(path: String): ItemStack = apply {
        idConsumer.accept(this, SkyOcean.id(path))
    }

    fun Item.withCatharsisId(path: String): ItemStack = defaultInstance.apply {
        idConsumer.accept(this, SkyOcean.id(path))
    }

    fun isModElementHidden(element: String): Boolean {
        return hiddenModElementsProvider.test(element)
    }

    @ApiDebug("Catharsis Support")
    internal fun debug(builder: DebugBuilder) = with(builder) {
        field("Catharsis Installed", KnownMods.CATHARSIS.installed)
        field(::hasIdConsumer)
        field(::hasDisabledConsumer)
        field(::hasHiddenModElementsProvider)
    }
}
