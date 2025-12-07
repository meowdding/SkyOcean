package me.owdding.skyocean.features.solvers.experiment

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.add
import me.owdding.skyocean.config.features.misc.ExperimentationTableConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifiers
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.extensions.get
import me.owdding.skyocean.utils.extensions.set
import me.owdding.skyocean.utils.items.ItemAttachmentKey
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.SlotClickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.mixins.accessors.ContainerScreenAccessor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
@ItemModifier
object UltrasequencerSolver : AbstractItemModifier() {
    private var currentNumber = 0
    private var numbers: MutableMap<Int, Int> = mutableMapOf()

    private var state = State.NONE
        set(value) {
            if (field == value) return
            field = value
            when (value) {
                State.NONE, State.REMEMBER -> {
                    numbers.clear()
                    currentNumber = 0
                }

                else -> {}
            }
        }
    private var lastVariant: Variant = Variant.METAPHYSICAL

    private val itemNumberKey = ItemAttachmentKey.int("ultrasequencer_item_number")

    override val displayName: Component = +ExperimentationTableConfig.ULTRASEQUENCER_TRANSLATION_KEY
    override val isEnabled: Boolean get() = ExperimentationTableConfig.ultrasequencerSolver

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onContainerChange(event: ContainerInitializedEvent) {
        if (!isUltrasequencerScreen(event.title)) return
        this.lastVariant = Variant.byName(event.title)
    }

    @Subscription
    @Suppress("DuplicatedCode")
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun screenTick(event: ContainerInitializedEvent) {
        if (!isUltrasequencerScreen(event.title)) return
        val size = (event.screen as? ContainerScreenAccessor)?.containerRows?.times(9) ?: 0
        ScreenEvents.afterTick(event.screen).register {
            event.screen.menu.slots.take(size).forEach { slot ->
                if (slot.item in Items.GLOWSTONE) state = State.REMEMBER
                if (slot.item in Items.CLOCK) state = State.RECALL

                evaluateSlot(slot.index, slot.item)
            }
        }
    }

    @Subscription(ContainerCloseEvent::class)
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onInventoryClose() {
        this.state = State.NONE
        this.numbers.clear()
    }

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onContainerChange(event: InventoryChangeEvent) {
        if (!isUltrasequencerScreen(event.title)) return
        evaluateSlot(event.slot.index, event.item)
    }

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onSlotClick(event: SlotClickEvent) {
        if (!isUltrasequencerScreen(event.title)) return
        if (this.state != State.RECALL) return
        val item = event.item
        if (McScreen.isControlDown) return
        val number = item[itemNumberKey] ?: return
        if (number != -1 && number == this.currentNumber + 1) this.currentNumber++ else event.cancel()
    }

    private fun evaluateSlot(index: Int, item: ItemStack) {
        if (state == State.REMEMBER && item.item !in ItemTag.GLASS_PANES && !item.isEmpty) {
            this.numbers[index] = if (item.count == 0) 1 else item.count
        } else if (state == State.RECALL) {
            val row = index / 9
            val column = index % 9
            if (row < 1 || row > this.lastVariant.rows || column < this.lastVariant.padding || column > 8 - this.lastVariant.padding) return
            val count = this.numbers[index] ?: -1
            item[itemNumberKey] = count
            ItemModifiers.clear(item)
            ItemModifiers.tryModify(item)
        }
    }

    private fun isUltrasequencerScreen(title: String? = McScreen.asMenu?.title?.stripped): Boolean =
        title != null && title.startsWith("Ultrasequencer") && !title.contains("➜")

    override fun appliesTo(itemStack: ItemStack): Boolean = isUltrasequencerScreen()

    override fun itemCountOverride(itemStack: ItemStack): Component? {
        val number = itemStack[itemNumberKey]?.takeUnless { it == -1 } ?: return null
        return Text.of(number.toString())
    }

    override fun itemOverride(itemStack: ItemStack): Item? {
        val number = itemStack[itemNumberKey] ?: return null
        if (number == -1) return Items.GRAY_STAINED_GLASS_PANE

        val difference = number - this.currentNumber
        return when {
            difference == 1 -> Items.GREEN_TERRACOTTA
            difference == 2 -> Items.YELLOW_TERRACOTTA
            difference == 3 -> Items.RED_TERRACOTTA
            difference <= 0 -> Items.BLACK_STAINED_GLASS
            else -> Items.GRAY_STAINED_GLASS_PANE
        }
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result {
        val itemNumber = item[itemNumberKey] ?: return Result.unmodified

        list.clear()
        list.add {
            if (itemNumber - this@UltrasequencerSolver.currentNumber == 1) {
                append("Correct Item!")
                this.color = TextColor.GREEN
            } else {
                append("Click blocked (ctrl to bypass)!")
                this.color = TextColor.RED
            }
        }

        return Result.consume
    }

    private enum class State {
        REMEMBER,
        RECALL,
        NONE,
    }

    private enum class Variant(val rows: Int, val padding: Int, val names: List<String>) {
        SUPREME(3, 1, "supreme"),
        LARGE(4, 1, "transcendent"),
        METAPHYSICAL(4, 0, "metaphysical"),
        ;

        constructor(rows: Int, padding: Int, vararg name: String) : this(rows, padding, name.toList())

        companion object Companion {
            val VALUES = entries
            fun byName(name: String) = name.removeSurrounding("Ultrasequencer (", ")").lowercase().let { name ->
                VALUES.find { name in it.names } ?: LARGE
            }
        }
    }
}
