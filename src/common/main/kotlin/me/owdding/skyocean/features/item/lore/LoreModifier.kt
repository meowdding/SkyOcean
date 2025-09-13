package me.owdding.skyocean.features.item.lore

import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.skyocean.generated.SkyOceanLoreModifiers
import me.owdding.skyocean.utils.ChatUtils
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.minecraft.ui.GatherItemTooltipComponentsEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ItemTooltipEvent
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.peek
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.reflect.full.findAnnotation

abstract class AbstractLoreModifier {

    abstract val displayName: Component?
    open val extraNames: List<Component>? = null
    abstract val isEnabled: Boolean
    abstract fun appliesTo(item: ItemStack): Boolean

    /** Return true if modified */
    abstract fun modify(item: ItemStack, list: MutableList<Component>): Boolean

    protected fun withMerger(original: MutableList<Component>, init: ListMerger<Component>.() -> Boolean): Boolean {
        val merger = ListMerger(original)
        val modified = merger.init()
        merger.addRemaining()
        original.clear()
        original.addAll(merger.destination)
        return modified
    }

    protected fun withComponentMerger(original: MutableList<ClientTooltipComponent>, init: ListMerger<ClientTooltipComponent>.() -> Unit) {
        val merger = ListMerger(original)
        merger.init()
        merger.addRemaining()
        original.clear()
        original.addAll(merger.destination)
    }

    open fun appendComponents(item: ItemStack, list: MutableList<ClientTooltipComponent>) {}

    protected fun ListMerger<Component>.addAllTillSpace() {
        while (index + 1 < original.size && peek().stripped.isNotBlank()) {
            read()
        }
        if (index + 1 < original.size) read()
    }

    protected fun ListMerger<Component>.space() = add(CommonComponents.EMPTY)
    protected fun ListMerger<Component>.add(init: MutableComponent.() -> Unit) = add(Text.of(init))
}

@Module
object LoreModifiers {
    val modifiers: List<AbstractLoreModifier> = SkyOceanLoreModifiers.collected.sortedBy {
        it::class.findAnnotation<LoreModifier>()?.priority ?: 0
    }.toList()

    @Subscription
    private fun ItemTooltipEvent.onLore() = tooltip.takeUnless { it.isEmpty() }?.let {
        val modified = modifiers.filter { it.isEnabled && it.appliesTo(this.item) && it.modify(item, this.tooltip) }

        if (modified.isEmpty()) return null

        this.tooltip.addFirst(Text.join(ChatUtils.ICON_SPACE_COMPONENT, this.tooltip.removeFirst()))
        if (Screen.hasShiftDown()) {
            this.tooltip.addAll(
                TooltipBuilder().apply {
                    space()
                    add("Lore modifiers active on item:") {
                        this.color = ChatUtils.DARK_OCEAN_BLUE
                    }
                    modified.forEach {
                        fun addName(name: Component) {
                            add {
                                append("- ")
                                append(name)
                                this.color = TextColor.GRAY
                            }
                        }
                        it.displayName?.let(::addName)
                        it.extraNames?.forEach(::addName)
                    }
                    space()
                }.lines(),
            )
        }
    }

    @Subscription
    private fun GatherItemTooltipComponentsEvent.onComponents() = components.takeUnless { it.isEmpty() }?.let {
        modifiers.filter { it.isEnabled && it.appliesTo(this.item) }.peek { it.appendComponents(item, this.components) }
    }
}

@AutoCollect("LoreModifiers")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class LoreModifier(
    /** Sorted ascending */
    val priority: Int = 0,
)
