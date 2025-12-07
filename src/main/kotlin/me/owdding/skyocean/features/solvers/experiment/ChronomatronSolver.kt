package me.owdding.skyocean.features.solvers.experiment

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.misc.ExperimentationTableConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifiers
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.extensions.get
import me.owdding.skyocean.utils.extensions.set
import me.owdding.skyocean.utils.items.ItemAttachmentKey
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.tags.ItemTags
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
import tech.thatgravyboat.skyblockapi.mixins.accessors.ContainerScreenAccessor
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.LinkedList

@Module
@ItemModifier
object ChronomatronSolver : AbstractItemModifier() {

    private val replacements = Object2ObjectArrayMap<Item, Item>().apply {
        put(Items.RED_STAINED_GLASS, Items.RED_SHULKER_BOX)
        put(Items.BLUE_STAINED_GLASS, Items.BLUE_SHULKER_BOX)
        put(Items.LIME_STAINED_GLASS, Items.LIME_SHULKER_BOX)
        put(Items.YELLOW_STAINED_GLASS, Items.YELLOW_SHULKER_BOX)
        put(Items.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_SHULKER_BOX)
        put(Items.PINK_STAINED_GLASS, Items.PINK_SHULKER_BOX)
        put(Items.GREEN_STAINED_GLASS, Items.GREEN_SHULKER_BOX)
        put(Items.CYAN_STAINED_GLASS, Items.CYAN_SHULKER_BOX)
        put(Items.ORANGE_STAINED_GLASS, Items.ORANGE_SHULKER_BOX)
        put(Items.PURPLE_STAINED_GLASS, Items.PURPLE_SHULKER_BOX)
        put(Items.RED_TERRACOTTA, Items.RED_SHULKER_BOX)
        put(Items.BLUE_TERRACOTTA, Items.BLUE_SHULKER_BOX)
        put(Items.LIME_TERRACOTTA, Items.LIME_SHULKER_BOX)
        put(Items.YELLOW_TERRACOTTA, Items.YELLOW_SHULKER_BOX)
        put(Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_SHULKER_BOX)
        put(Items.PINK_TERRACOTTA, Items.PINK_SHULKER_BOX)
        put(Items.GREEN_TERRACOTTA, Items.GREEN_SHULKER_BOX)
        put(Items.CYAN_TERRACOTTA, Items.CYAN_SHULKER_BOX)
        put(Items.ORANGE_TERRACOTTA, Items.ORANGE_SHULKER_BOX)
        put(Items.PURPLE_TERRACOTTA, Items.PURPLE_SHULKER_BOX)
    }
    private val futureReplacements = Object2ObjectArrayMap<Item, Item>().apply {
        put(Items.RED_STAINED_GLASS, Items.RED_STAINED_GLASS)
        put(Items.BLUE_STAINED_GLASS, Items.BLUE_STAINED_GLASS)
        put(Items.LIME_STAINED_GLASS, Items.LIME_STAINED_GLASS)
        put(Items.YELLOW_STAINED_GLASS, Items.YELLOW_STAINED_GLASS)
        put(Items.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_STAINED_GLASS)
        put(Items.PINK_STAINED_GLASS, Items.PINK_STAINED_GLASS)
        put(Items.GREEN_STAINED_GLASS, Items.GREEN_STAINED_GLASS)
        put(Items.CYAN_STAINED_GLASS, Items.CYAN_STAINED_GLASS)
        put(Items.ORANGE_STAINED_GLASS, Items.ORANGE_STAINED_GLASS)
        put(Items.PURPLE_STAINED_GLASS, Items.PURPLE_STAINED_GLASS)
        put(Items.RED_TERRACOTTA, Items.RED_STAINED_GLASS)
        put(Items.BLUE_TERRACOTTA, Items.BLUE_STAINED_GLASS)
        put(Items.LIME_TERRACOTTA, Items.LIME_STAINED_GLASS)
        put(Items.YELLOW_TERRACOTTA, Items.YELLOW_STAINED_GLASS)
        put(Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_STAINED_GLASS)
        put(Items.PINK_TERRACOTTA, Items.PINK_STAINED_GLASS)
        put(Items.GREEN_TERRACOTTA, Items.GREEN_STAINED_GLASS)
        put(Items.CYAN_TERRACOTTA, Items.CYAN_STAINED_GLASS)
        put(Items.ORANGE_TERRACOTTA, Items.ORANGE_STAINED_GLASS)
        put(Items.PURPLE_TERRACOTTA, Items.PURPLE_STAINED_GLASS)
    }

    private var state = State.NONE
        set(value) {
            if (field == value) return
            field = value
            when (value) {
                State.NONE, State.REMEMBER -> {
                    queue.clear()
                }

                else -> {}
            }
        }

    private val correctItemKey = ItemAttachmentKey.boolean("chronomatron_correct_item")
    private val mainItemKey = ItemAttachmentKey.of<Item>("chronomatron_item_replacement")

    private val queue = LinkedList<Int>()
    private var slotStates: Int = 0
    private var lastVariant: Variant = Variant.SMALL

    override val displayName: Component = +ExperimentationTableConfig.CHRONOMATRON_TRANSLATION_KEY
    override val isEnabled: Boolean get() = ExperimentationTableConfig.chronomatronSolver

    fun Int.decompose(): Triple<Int, Int, Int> {
        val variantRows = this@ChronomatronSolver.lastVariant.rows
        val row = this / 9 + (variantRows - 1)
        val column = this % 9
        val slotStateIndex = (row / variantRows - 1) * 9 + column
        return Triple(row, column, slotStateIndex)
    }

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onContainerChange(event: ContainerInitializedEvent) {
        if (!isChronomatronScreen(event.title)) return
        this.lastVariant = Variant.byName(event.title)
    }

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onContainerChange(event: InventoryChangeEvent) {
        if (!isChronomatronScreen(event.title)) return

        val size = (event.screen as? ContainerScreenAccessor)?.containerRows?.times(9) ?: 0
        if (event.item in Items.GLOWSTONE) state = State.REMEMBER
        if (event.item in Items.CLOCK) {
            state = State.RECALL
            event.inventory.take(size).forEach { evaluateSlot(it.index, it.item) }
        }

        evaluateSlot(event.slot.index, event.item)
    }

    @Subscription
    @Suppress("DuplicatedCode")
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun screenTick(event: ContainerInitializedEvent) {
        if (!isChronomatronScreen(event.title)) return
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
        this.slotStates = 0
    }

    private fun evaluateSlot(index: Int, item: ItemStack) {
        val (row, _, slotStateIndex) = index.decompose()
        val slotStateBitMask = 1 shl slotStateIndex
        if (item in ItemTags.TERRACOTTA) {
            if (slotStates and slotStateBitMask == 0 && row % 2 == 1) {
                if (state == State.REMEMBER) {
                    queue.add(slotStateIndex)
                }
                slotStates = slotStates or slotStateBitMask
            }
        } else {
            slotStates = slotStates and slotStateBitMask.inv()
        }
        if (state == State.RECALL) {
            val correct = queue.peekFirst() == slotStateIndex
            val correctInFuture = queue.getOrNull(1) == slotStateIndex
            if (!replacements.containsKey(item.item)) return

            item[correctItemKey] = correct

            item[mainItemKey] = if (correct) replacements.getOrElse(item.item) { item.item }
            else if (correctInFuture) futureReplacements.getOrElse(item.item) { item.item }
            else Items.GRAY_STAINED_GLASS

            ItemModifiers.tryModify(item)
        }
    }

    @Subscription
    @MustBeContainer
    fun onSlotClick(event: SlotClickEvent) {
        if (!isChronomatronScreen(event.title)) return
        if (this.state != State.RECALL) return
        val item = event.item
        if (McScreen.isControlDown) return
        if (item[correctItemKey] == null) return
        if (item[correctItemKey] == true) this.queue.pop() else event.cancel()
    }

    private fun isChronomatronScreen(title: String? = McScreen.asMenu?.title?.stripped): Boolean =
        title != null && title.startsWith("Chronomatron") && !title.contains("➜")

    override fun appliesTo(itemStack: ItemStack): Boolean = isChronomatronScreen()

    override fun itemOverride(itemStack: ItemStack): Item? = itemStack[this.mainItemKey]

    override fun getExtraComponents(itemStack: ItemStack): DataComponentPatch? {
        val isCorrect = itemStack[correctItemKey] ?: return null
        return DataComponentPatch.builder()
            .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, isCorrect)
            .build()
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result = withMerger(list) {
        addRemaining()
        if (item[correctItemKey] != false) return@withMerger Result.unmodified

        add("Click blocked! (ctrl to bypass)") {
            this.color = TextColor.RED
        }

        Result.modified
    }

    private enum class State {
        REMEMBER,
        RECALL,
        NONE,
    }

    private enum class Variant(val rows: Int, val names: List<String>) {
        SMALL(
            3,
            "high",
            "grand",
            "supreme",
        ),
        LARGE(2),
        ;

        constructor(rows: Int, vararg name: String) : this(rows, name.toList())

        companion object Companion {
            val VALUES = entries
            fun byName(name: String) = name.removeSurrounding("Chronomatron (", ")").lowercase().let { name ->
                VALUES.find { name in it.names } ?: LARGE
            }
        }
    }
}
