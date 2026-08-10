package me.owdding.skyocean.compat

import me.owdding.lib.utils.KnownMods
import me.owdding.skyocean.ApiDebug
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.DefaultedValue
import me.owdding.skyocean.utils.debug.DebugBuilder
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.BiConsumer
import java.util.function.Predicate

object CatharsisSupport {

    private var idConsumer: DefaultedValue<Any, BiConsumer<ItemStack, Identifier>> = DefaultedValue(BiConsumer { _, _ -> })
    private var disabledConsumer: DefaultedValue<Any, BiConsumer<ItemStack, Boolean>> = DefaultedValue(BiConsumer { _, _ -> })
    private var hiddenModElementsProvider: DefaultedValue<Any, Predicate<String>> = DefaultedValue(Predicate { false })

    @JvmStatic
    fun id(consumer: BiConsumer<ItemStack, Identifier>) = this.idConsumer.setValue(consumer)
    @JvmStatic
    fun disabled(consumer: BiConsumer<ItemStack, Boolean>) = this.disabledConsumer.setValue(consumer)
    @JvmStatic
    fun hiddenModElements(provider: Predicate<String>)  = this.hiddenModElementsProvider.setValue(provider)

    fun ItemStack.disableCatharsisModifications() = apply {
        disabledConsumer().accept(this, true)
    }

    fun ItemStack.withCatharsisId(path: String): ItemStack = apply {
        idConsumer().accept(this, SkyOcean.id(path))
    }

    fun Item.withCatharsisId(path: String): ItemStack = defaultInstance.apply {
        idConsumer().accept(this, SkyOcean.id(path))
    }

    fun isModElementHidden(element: String): Boolean {
        return hiddenModElementsProvider().test(element)
    }

    @ApiDebug("Catharsis Support")
    internal fun debug(builder: DebugBuilder) = with(builder) {
        field("Catharsis Installed", KnownMods.CATHARSIS.installed)
        field(::idConsumer)
        field(::disabledConsumer)
        field(::hiddenModElementsProvider)
    }
}
