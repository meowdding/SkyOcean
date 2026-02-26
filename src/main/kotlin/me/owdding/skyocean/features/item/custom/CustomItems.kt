package me.owdding.skyocean.features.item.custom

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.accessors.customize.ItemStackAccessor
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.DataStorage
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.extentions.getTag
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@LateInitModule
object CustomItems : MeowddingLogger by SkyOcean.featureLogger() {

    private val map: MutableMap<ItemKey, CustomItemData> = mutableMapOf()
    val staticMap: MutableMap<ItemKey, CustomItemData> = mutableMapOf()

    private val vanillaIntegration: Cache<ItemKey, CustomItemData> = CacheBuilder.newBuilder()
        .expireAfterAccess(10.minutes.toJavaDuration())
        .expireAfterWrite(10.minutes.toJavaDuration())
        .maximumSize(500)
        .weakKeys()
        .build()

    private val storage: DataStorage<MutableList<CustomItemData>> = DataStorage(
        { mutableListOf() },
        "custom_items",
        CodecHelpers.mutableList(),
    )

    init {
        map.putAll(storage.get().associateBy { it.key })
    }

    fun modify(itemStack: ItemStack, init: context(ItemStack) CustomItemData.() -> Unit): Boolean {
        val key = itemStack.getKey() ?: return false
        context(itemStack) {
            getOrPut(key).init()
        }
        storage.save()
        return true
    }

    fun remove(key: ItemKey): Boolean {
        val removed = storage.get().remove(map.remove(key))
        if (removed) save()
        return removed
    }

    fun getOrPut(key: ItemKey) = map.getOrPut(key) {
        val data = CustomItemData(key)
        storage.get().add(data)
        storage.save()
        data
    }

    @OptIn(ExperimentalUuidApi::class)
    fun ItemStack.createKey(): ItemKey? = when {
        this.getTag("skyocean:customization_item") != null -> UuidKey(Uuid.fromLongs(0, 0).toJavaUuid())
        this.getTag("skyocean:static_item") != null -> UuidKey(
            this.getTag("skyocean:static_item")!!.asString().map { UUID.fromString(it) }.orElseGet { UUID.randomUUID() },
        )

        this[DataTypes.UUID] != null -> UuidKey(this[DataTypes.UUID]!!)
        this[DataTypes.TIMESTAMP] != null && this.getSkyBlockId() != null -> IdAndTimeKey(
            this.getSkyBlockId()!!,
            this[DataTypes.TIMESTAMP]!!.toEpochMilliseconds(),
        )
        this.getSkyBlockId() != null -> IdKey(this.getSkyBlockId()!!)

        else -> null
    }

    fun ItemStack.getKey(): ItemKey? = ItemStackAccessor.getItemKey(this)
    fun ItemStack.getCustomData() = map[this.getKey()]
    fun ItemStack.getVanillaIntegrationData() =
        this.getKey()?.let { vanillaIntegration.getIfPresent(it) }?.takeIf { MiscConfig.customizationVanillaIntegration }

    fun ItemStack.getStaticCustomData() = staticMap[this.getKey()]

    fun ItemStack.getOrTryCreateCustomData() = this.getKey()?.let { getOrPut(it) }
    fun ItemStack.getOrCreateStaticData() = this.getKey()?.let { staticMap.getOrPut(it) { CustomItemData(it) } }

    operator fun <T : Any> ItemStack.get(component: CustomItemComponent<T>): T? {
        return this.getCustomData()?.let { it[component] }
    }

    operator fun <T : Any> ItemStack.set(component: CustomItemComponent<T>, value: T?) {
        this.getOrTryCreateCustomData()?.let {
            if (value == null) {
                it.data.remove(component)
                return
            }
            it[component] = value
        }
    }

    fun loadVanilla(self: ItemStack, key: ItemKey?) {
        key ?: return
        val skin = self[DataTypes.HELMET_SKIN]?.let {
            runCatching { AnimatedSkyblockSkin(SkyBlockId.item(it.lowercase())) }.getOrNull()
        }
        val dye = self[DataTypes.APPLIED_DYE]?.let { dye ->
            runCatching { AnimatedSkyBlockDye(dye.lowercase()) }.getOrNull()
        }

        if (skin == null && dye == null) return
        vanillaIntegration.put(
            key,
            CustomItemData(key).apply {
                if (skin != null) {
                    this[CustomItemDataComponents.SKIN] = skin
                }
                if (dye != null) {
                    this[CustomItemDataComponents.COLOR] = dye
                }
            },
        )
    }

    fun save() = storage.save()
}
