package me.owdding.skyocean.features.item.custom

import me.owdding.ktmodules.Module
import me.owdding.skyocean.accessors.customize.ItemStackAccessor
import me.owdding.skyocean.api.SkyOceanItemId.Companion.getSkyOceanId
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.utils.CodecHelpers
import me.owdding.skyocean.utils.storage.DataStorage
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.extentions.get

/**
 * change item model
 * change name
 * trims maybe
 * color for leather armor for surte/sb dye integration would be awesome
 * head texture + skin textures/variants
 */
@Module
object CustomItems {

    private val map: MutableMap<ItemKey, CustomItemData> = mutableMapOf()

    private val storage: DataStorage<MutableList<CustomItemData>> = DataStorage(
        { mutableListOf() },
        "custom_items",
        CodecHelpers.list(),
    )

    init {
        map.putAll(storage.get().associateBy { it.key })
    }

    fun modify(itemStack: ItemStack, init: context(ItemStack) CustomItemData.() -> Unit): Boolean {
        val key = itemStack.createKey() ?: return false
        context(itemStack) {
            getOrPut(key).init()
        }
        storage.save()
        return true
    }

    fun remove(itemStack: ItemStack) {
        storage.get().remove(map.remove(itemStack.createKey()))
    }

    fun getOrPut(key: ItemKey) = map.getOrPut(key) {
        val data = CustomItemData(key)
        storage.get().add(data)
        storage.save()
        data
    }

    fun ItemStack.createKey(): ItemKey? = when {
        this[DataTypes.UUID] != null -> UuidKey(this[DataTypes.UUID]!!)
        this[DataTypes.TIMESTAMP] != null && this.getSkyOceanId() != null -> IdAndTimeKey(
            this.getSkyOceanId()!!,
            this[DataTypes.TIMESTAMP]!!.toEpochMilliseconds(),
        )

        else -> null
    }

    fun ItemStack.getKey(): ItemKey? = ItemStackAccessor.getItemKey(this)
    fun ItemStack.getCustomData() = map[this.getKey()]
    fun ItemStack.getOrTryCreateCustomData() = this.getKey()?.let { getOrPut(it) }

    operator fun <T> ItemStack.get(component: CustomItemComponent<T>): T? {
        return this.getCustomData()?.let { it[component] }
    }

    operator fun <T> ItemStack.set(component: CustomItemComponent<T>, value: T?) {
        this.getOrTryCreateCustomData()?.let {
            if (value == null) {
                it.data.remove(component)
                return
            }
            it[component] = value
        }
    }
}
