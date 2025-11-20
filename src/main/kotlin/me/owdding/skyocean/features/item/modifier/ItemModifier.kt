package me.owdding.skyocean.features.item.modifier

import earth.terrarium.olympus.client.utils.State
import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanItemModifiers
import me.owdding.skyocean.utils.SkyOceanModifyIndicator
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.extensions.or
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.intellij.lang.annotations.MagicConstant
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.minecraft.ui.GatherItemTooltipComponentsEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ItemTooltipEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerHotbarChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.PlayerInventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.getVisualItem
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.*

abstract class AbstractItemModifier {

    companion object {
        const val LOWEST = 1000000
        const val LOW = 100000
        const val NORMAL = 0
        const val HIGH = -100000
        const val HIGHEST = -1000000
    }

    @MagicConstant(valuesFromClass = Companion::class)
    open val priority: Int = NORMAL
    protected abstract val displayName: Component
    open val displayNames: List<Component> by lazy { listOf(displayName) }
    abstract val isEnabled: Boolean

    abstract fun appliesTo(itemStack: ItemStack): Boolean
    open fun appliesToScreen(screen: Screen) = true

    open fun itemCountOverride(itemStack: ItemStack): Component? = null
    open fun itemOverride(itemStack: ItemStack): Item? = null
    open fun backgroundItem(itemStack: ItemStack): ItemStack? = null
    open fun getExtraComponents(itemStack: ItemStack): DataComponentPatch? = null
    open fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result = Result.unmodified
    open fun appendComponents(item: ItemStack, list: MutableList<ClientTooltipComponent>): Result = Result.unmodified


    protected fun withMerger(original: MutableList<Component>, init: ListMerger<Component>.() -> Result?): Result {
        val merger = ListMerger(original)
        val result = merger.init()
        merger.addRemaining()
        original.clear()
        original.addAll(merger.destination)
        return result ?: Result.unmodified
    }

    protected fun withComponentMerger(original: MutableList<ClientTooltipComponent>, init: ListMerger<ClientTooltipComponent>.() -> Result?): Result {
        val merger = ListMerger(original)
        val result = merger.init()
        merger.addRemaining()
        original.clear()
        original.addAll(merger.destination)
        return result ?: Result.unmodified
    }

    protected fun ListMerger<Component>.skipUntilAfterSpace() {
        while (index + 1 < original.size && peek().stripped.isNotBlank()) {
            read()
        }
        if (index + 1 < original.size) read()
    }

    protected fun ListMerger<Component>.addUntilRarityLine(rarity: SkyBlockRarity): Boolean {
        val name = rarity.displayName.uppercase()
        val index = original.indexOfLast { it.stripped.contains(name) }
        if (index == -1) return false
        repeat(index + 1) { copy() }
        return true
    }

    protected fun ListMerger<Component>.space() = add(CommonComponents.EMPTY)
    protected fun ListMerger<Component>.add(init: MutableComponent.() -> Unit) = add(Text.of(init))
    protected fun ListMerger<Component>.addUntilAfter(predicate: (Component) -> Boolean) {
        addUntil(predicate)
        read()
    }

    protected fun ListMerger<Component>.copyAll() {
        while (canRead()) copy()
    }

    /**
     * @param modified Indicates whether there was any modification made
     * @param propagateFurther If true, lower priority modifiers will get a chance to modify as well, if false, no other modifier will be called.
     */
    @ConsistentCopyVisibility
    data class Result private constructor(
        val modified: Boolean,
        val propagateFurther: Boolean,
    ) {
        companion object Companion {
            val consume = Result(modified = true, propagateFurther = false)
            val modified = Result(modified = true, propagateFurther = true)
            val cancelled = Result(modified = false, propagateFurther = false)
            val unmodified = Result(modified = false, propagateFurther = true)
        }
    }
}

@Module
object ItemModifiers {

    val modifiers: List<AbstractItemModifier> = SkyOceanItemModifiers.collected.sortedBy { it.priority }.toList()
    val modifiedItems: WeakHashMap<ItemStack, List<Component>> = WeakHashMap()

    @Subscription
    @MustBeContainer
    fun InventoryChangeEvent.onContainerChange() {
        tryModify(item)
    }

    @Subscription
    fun PlayerInventoryChangeEvent.onInventoryChange() {
        tryModify(item)
    }

    @Subscription
    fun PlayerHotbarChangeEvent.onHotbarChange() {
        tryModify(item)
    }

    private fun tryModify(itemStack: ItemStack) {
        if (modifiedItems.contains(itemStack)) return

        val modifiers = modifiers.filter { it.isEnabled && it.appliesTo(itemStack) && McScreen.self?.let { screen -> it.appliesToScreen(screen) } == true }

        if (modifiers.isEmpty()) return

        val map = mutableMapOf<DataMarker<*>, Any>()
        val usedModifiers = mutableListOf<Component>()
        context(map, itemStack) {
            for (modifier in modifiers) {
                val state = State.of(false)
                context(state) {
                    modifier.extract(DataMarker.ITEM, AbstractItemModifier::itemOverride)
                    modifier.extract(DataMarker.BACKGROUND_ITEM, AbstractItemModifier::backgroundItem)
                    modifier.extract(DataMarker.ITEM_COUNT, AbstractItemModifier::itemCountOverride)
                    modifier.getExtraComponents(itemStack)?.entrySet()?.forEach { (key, value) ->
                        if (value.isEmpty) return@forEach
                        set(DataMarker.ComponentDataMarker(key), value.get())
                    }
                }
                if (state.get()) {
                    usedModifiers.addAll(modifier.displayNames)
                }
            }
        }

        itemStack.skyoceanReplace(addIndicator = false) {
            for ((key, value) in map) {
                when (key) {
                    DataMarker.ITEM -> item = value.unsafeCast()
                    DataMarker.BACKGROUND_ITEM -> backgroundItem = value.unsafeCast()
                    DataMarker.ITEM_COUNT -> count = 2
                    is DataMarker.ComponentDataMarker -> {
                        set(key.component, value.unsafeCast())
                    }

                    else -> {}
                }
            }
        }
        modifiedItems[itemStack.getVisualItem() ?: itemStack] = usedModifiers
    }

    private context(map: MutableMap<DataMarker<*>, Any>, state: State<Boolean>, itemStack: ItemStack) fun <T : Any> AbstractItemModifier.extract(
        dataMarker: DataMarker<T>,
        mapper: AbstractItemModifier.(ItemStack) -> T?,
    ) {
        if (map.contains(dataMarker)) return
        set(dataMarker, mapper(itemStack))
    }

    context(map: MutableMap<DataMarker<*>, Any>, state: State<Boolean>) private fun <T : Any, V> set(dataMarker: DataMarker<T>, value: V?) {
        if (map.contains(dataMarker)) return
        if (value != null) {
            map[dataMarker] = value
            state.or(true)
        }
    }

    private sealed interface DataMarker<T> {
        companion object {
            val ITEM = DefaultDataMarker<Item>()
            val BACKGROUND_ITEM = DefaultDataMarker<ItemStack>()
            val ITEM_COUNT = DefaultDataMarker<Component>()
        }

        class DefaultDataMarker<T> : DataMarker<T>
        data class ComponentDataMarker<T>(val component: DataComponentType<T>) : DataMarker<T>
    }

    @Subscription
    private fun ItemTooltipEvent.onLore() = tooltip.takeUnless { it.isEmpty() }?.let {
        val loreModifiers = modifiers.filter {
            it.isEnabled && it.appliesTo(item) && McScreen.self?.let { screen -> it.appliesToScreen(screen) } == true
        }

        val usedLoreModifiers: MutableList<AbstractItemModifier> = mutableListOf()
        var result: AbstractItemModifier.Result? = null
        for (modifier in loreModifiers) {
            result = modifier.modifyTooltip(item, tooltip, result)
            if (result.modified) usedLoreModifiers.add(modifier)
            if (!result.propagateFurther) break
        }

        val standardModifiers = this@ItemModifiers.modifiedItems.getOrDefault(item, emptyList())
        if (usedLoreModifiers.isEmpty() && standardModifiers.isEmpty()) {
            return null
        }

        when (Config.modifyIndicator) {
            SkyOceanModifyIndicator.PREFIX -> this.tooltip.addFirst(Text.join(ChatUtils.ICON_SPACE_COMPONENT, this.tooltip.removeFirst()))
            SkyOceanModifyIndicator.SUFFIX -> this.tooltip.addFirst(Text.join(this.tooltip.removeFirst(), ChatUtils.SPACE_ICON_COMPONENT))
            SkyOceanModifyIndicator.LORE -> {
                this.tooltip.add(1, Text.of("Modified by SkyOcean").withColor(TextColor.DARK_GRAY))
                this.tooltip.add(2, CommonComponents.EMPTY)
            }

            SkyOceanModifyIndicator.NOTHING -> {}
        }

        if (McScreen.isShiftDown) {
            this.tooltip.add(!"General modifiers active on item:", standardModifiers.filterNotNull())
            this.tooltip.add(!"Lore modifiers active on item:", usedLoreModifiers.flatMap { it.displayNames })
            if (standardModifiers.isNotEmpty() || usedLoreModifiers.isNotEmpty()) {
                this.tooltip.add(CommonComponents.EMPTY)
            }
        }
    }

    private fun MutableList<Component>.add(message: Component, modifiers: List<Component>) {
        if (modifiers.isEmpty()) return
        addAll(
            TooltipBuilder().apply {
                space()
                add {
                    append(message)
                    this.color = OceanColors.DARK_CYAN_BLUE
                }
                modifiers.forEach {
                    add {
                        append("- ")
                        append(it)
                        this.color = TextColor.GRAY
                    }
                }
            }.lines(),
        )
    }

    @Subscription
    private fun GatherItemTooltipComponentsEvent.onComponents() = components.takeUnless { it.isEmpty() }?.let {
        var result: AbstractItemModifier.Result? = null
        for (modifier in modifiers.filter {
            it.isEnabled && it.appliesTo(this.item) && McScreen.self?.let { screen ->
                it.appliesToScreen(screen)
            } == true
        }) {
            result = modifier.appendComponents(item, components)
            if (!result.propagateFurther) break
        }
    }
}

@AutoCollect("ItemModifiers")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ItemModifier
