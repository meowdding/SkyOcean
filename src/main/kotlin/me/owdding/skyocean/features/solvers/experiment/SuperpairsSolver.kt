package me.owdding.skyocean.features.solvers.experiment

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import me.owdding.ktmodules.Module
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
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
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
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.mixins.accessors.ContainerScreenAccessor
import tech.thatgravyboat.skyblockapi.platform.properties
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
@ItemModifier
object SuperpairsSolver : AbstractItemModifier() {
    override val displayName: Component = +ExperimentationTableConfig.SUPEPAIRS_TRANSLATION_KEY
    override val isEnabled: Boolean get() = ExperimentationTableConfig.superpairsSolver

    private val superPairSlots: MutableSet<Int> = mutableSetOf()
    private val superPairItems = Int2ObjectArrayMap<ItemStack>()

    private val itemKey = ItemAttachmentKey.of<ItemStack>("superpairs_actual_item")
    private val itemIndexKey = ItemAttachmentKey.int("superpairs_index")

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onContainerChange(event: InventoryChangeEvent) {
        if (!isSuperpairsScreen(event.title)) return
        evaluateSlot(event.slot.index, event.item)
    }

    @Subscription(ContainerCloseEvent::class)
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onInventoryClose() {
        this.superPairSlots.clear()
        this.superPairItems.clear()
    }

    @Subscription
    @Suppress("DuplicatedCode")
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun screenTick(event: ContainerInitializedEvent) {
        if (!isSuperpairsScreen(event.title)) return
        val size = (event.screen as? ContainerScreenAccessor)?.containerRows?.times(9) ?: 0
        ScreenEvents.afterTick(event.screen).register {
            event.screen.menu.slots.take(size).forEach { slot ->
                evaluateSlot(slot.index, slot.item)
            }
        }
    }

    fun evaluateSlot(index: Int, item: ItemStack) {
        if (item.isEmpty) return
        if (item in Items.CYAN_STAINED_GLASS) {
            this.superPairSlots.add(index)
            if (this.superPairItems.containsKey(index)) {
                item[itemKey] = this.superPairItems.get(index)
                item[itemIndexKey] = index
            }
            ItemModifiers.clear(item)
            ItemModifiers.tryModify(item)
            return
        }

        if (this.superPairItems.containsKey(index)) return
        this.superPairItems.put(index, item.copy())
    }

    private fun isSuperpairsScreen(title: String? = McScreen.asMenu?.title?.stripped): Boolean =
        title != null && title.startsWith("Superpairs") && !title.contains("➜")

    private fun ItemStack.shouldOverride(): Boolean {
        this[itemKey]?.takeUnless { it.isEmpty } ?: return false
        return this in Items.CYAN_STAINED_GLASS
    }

    private fun ItemStack.getSkinInformation() = this.get(DataComponents.PROFILE)?.properties?.get("textures")?.firstOrNull()?.value

    fun hasMatchingItem(index: Int): Boolean {
        val item = this.superPairItems.get(index) ?: return false

        return this.superPairItems.filter { (valueIndex, value) ->
            valueIndex != index &&
                item.hoverName == value.hoverName &&
                item.item == value.item &&
                item.getRawLore().joinToString().stripColor() == value.getRawLore().joinToString().stripColor() &&
                item.getSkinInformation() == value.getSkinInformation()
        }.values.any()
    }

    override fun backgroundItem(itemStack: ItemStack): ItemStack? = if (itemStack.shouldOverride()) {
        if (hasMatchingItem((itemStack[itemIndexKey] ?: -1))) Items.ORANGE_STAINED_GLASS_PANE.defaultInstance else Items.CYAN_STAINED_GLASS_PANE.defaultInstance
    } else null

    override fun itemOverride(itemStack: ItemStack): Item? {
        if (!itemStack.shouldOverride()) return null
        val item = itemStack[this.itemKey]
        return item?.item
    }

    override fun getExtraComponents(itemStack: ItemStack): DataComponentPatch? {
        if (!itemStack.shouldOverride()) return null
        return itemStack[this.itemKey]?.componentsPatch
    }

    override fun appliesTo(itemStack: ItemStack): Boolean = isSuperpairsScreen()
}
