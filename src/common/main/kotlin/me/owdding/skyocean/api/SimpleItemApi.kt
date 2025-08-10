package me.owdding.skyocean.api

import me.owdding.lib.extensions.toReadableTime
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId.Companion.UNKNOWN
import me.owdding.skyocean.api.SkyOceanItemId.Companion.attribute
import me.owdding.skyocean.api.SkyOceanItemId.Companion.enchantment
import me.owdding.skyocean.api.SkyOceanItemId.Companion.item
import me.owdding.skyocean.api.SkyOceanItemId.Companion.pet
import me.owdding.skyocean.api.SkyOceanItemId.Companion.rune
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.Utils.sanitizeForCommandInput
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.PetQuery
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoRunesAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since

@LateInitModule
object SimpleItemApi : MeowddingLogger by SkyOcean.featureLogger() {

    private val cache: MutableMap<SkyOceanItemId, ItemStack?> = mutableMapOf()
    private val nameCache: MutableMap<String, SkyOceanItemId> = mutableMapOf()

    init {
        val start = currentInstant()
        RepoAPI.pets().pets().entries.associate { (id, data) -> data.name() to pet(id) }
            .let(nameCache::putAll)

        RepoAPI.runes().runes().entries.flatMap { (id, data) ->
            data.map { rune ->
                rune.name().stripColor() to rune("$id:${rune.tier()}")
            }
        }.toMap().let(nameCache::putAll)

        RepoAPI.enchantments().enchantments().flatMap { (id, enchantments) ->
            enchantments.levels().map { (level, enchantment) ->
                "${enchantments.name()} ${enchantment.literalLevel()}" to enchantment("$id:${enchantment.level()}")
            }
        }.toMap().let(nameCache::putAll)

        RepoAPI.attributes().attributes().flatMap { (id, attribute) ->
            listOf(
                attribute.name() to attribute(attribute.id()),
                attribute.shardName() to attribute(attribute.id()),
            )
        }.toMap().let(nameCache::putAll)

        RepoAPI.items().items().entries.mapNotNull { (id, element) ->
            val components =
                element.getPath("['components'].['minecraft:custom_name'].['text']") ?: return@mapNotNull null
            components.asString.stripColor() to item(id)
        }.toMap().let(nameCache::putAll)

        val newCache = nameCache.mapKeys { (key) -> key.lowercase().stripColor() }
            .entries.flatMap { (key, value) ->
                listOf(
                    key to value,
                    key.sanitizeForCommandInput() to value,
                )
            }.distinct().toMap()
        nameCache.clear()
        nameCache.putAll(newCache)
        trace("Cached ${nameCache.size} item names in ${start.since().toReadableTime(allowMs = true)}")
    }

    fun findIdByName(name: String) = nameCache[name.lowercase().stripColor()]

    fun getItemByIdOrNull(id: SkyOceanItemId): ItemStack? = cache.getOrPut(id.trySafe(::item)) {
        val itemId = id.cleanId.uppercase()

        if (itemId == UNKNOWN) {
            return null
        }

        return RepoItemsAPI.getItemOrNull(itemId)
    }

    fun getItemById(id: SkyOceanItemId): ItemStack = getItemByIdOrNull(id) ?: itemBuilder(Items.BARRIER) {
        name("Unknown item: $id")
    }

    fun getPetByIdOrNull(id: SkyOceanItemId): ItemStack? = cache.getOrPut(id.trySafe(::pet)) {
        val petId = id.cleanId.uppercase()

        if (petId == UNKNOWN) {
            return@getOrPut null
        }

        if (petId.contains(":")) {
            val (petId, rarity) = petId.split(":")
            val sbRarity = runCatching { SkyBlockRarity.valueOf(rarity) }.getOrNull()
            val pet = runCatching {
                sbRarity?.let { RepoPetsAPI.getPetAsItemOrNull(PetQuery(petId, it, 1)) }
            }.getOrNull()
            pet?.let { return@getOrPut it }
        }

        return@getOrPut SkyBlockRarity.entries.reversed().firstNotNullOfOrNull { skyBlockRarity ->
            runCatching {
                RepoPetsAPI.getPetAsItemOrNull(PetQuery(petId.substringBefore(":"), skyBlockRarity, 1))
            }.getOrNull()
        }
    }

    fun getPetById(id: SkyOceanItemId): ItemStack = getPetByIdOrNull(id) ?: itemBuilder(Items.BARRIER) {
        name("Unknown pet: $id")
    }

    fun getRuneByIdOrNull(id: SkyOceanItemId): ItemStack? = cache.getOrPut(id.trySafe(::rune)) {
        val runeId = id.cleanId.uppercase()

        if (runeId == UNKNOWN) {
            return@getOrPut null
        }

        if (runeId.contains(":")) {
            val (runeId, literalLevel) = runeId.split(":")
            val level = literalLevel.toIntValue()
            RepoRunesAPI.getRuneAsItemOrNull(runeId, level)?.let { return@getOrPut it }
        }

        for (i in 3 downTo 0) {
            RepoRunesAPI.getRuneAsItemOrNull(runeId.substringBefore(":"), i)?.let { return@getOrPut it }
        }

        return@getOrPut null
    }

    fun getRuneById(id: SkyOceanItemId) = getRuneByIdOrNull(id) ?: itemBuilder(Items.BARRIER) {
        name("Unknown rune: $id")
    }

    fun getEnchantmentByIdOrNull(id: SkyOceanItemId): ItemStack? = cache.getOrPut(id.trySafe(::enchantment)) {
        val enchantmentId = id.cleanId.uppercase()

        if (enchantmentId == UNKNOWN) {
            return@getOrPut null
        }

        if (enchantmentId.contains(":")) {
            val (enchantmentId, literalLevel) = enchantmentId.split(":")
            val level = literalLevel.toIntValue()
            EnchantmentApi.getEnchantmentAsItemOrNull(enchantmentId, level)?.let { return@getOrPut it }
        }

        for (i in 10 downTo 0) {
            EnchantmentApi.getEnchantmentAsItemOrNull(enchantmentId.substringBefore(":"), i)?.let { return@getOrPut it }
        }

        return@getOrPut null
    }

    fun getEnchantmentById(id: SkyOceanItemId): ItemStack = getEnchantmentByIdOrNull(id) ?: itemBuilder(Items.BARRIER) {
        name("Unknown enchantment: $id")
    }

    fun getAttributeByIdOrNull(id: SkyOceanItemId): ItemStack? = cache.getOrPut(id.trySafe(::attribute)) {
        val attributeId = id.cleanId.uppercase()

        if (attributeId == UNKNOWN) {
            return@getOrPut null
        }

        AttributeApi.getAttributeByIdOrNull(attributeId)?.let { return@getOrPut it }

        return@getOrPut null
    }

    fun getAttributeById(id: SkyOceanItemId): ItemStack = getAttributeByIdOrNull(id) ?: itemBuilder(Items.BARRIER) {
        name("Unknown attribute: $id")
    }

}
