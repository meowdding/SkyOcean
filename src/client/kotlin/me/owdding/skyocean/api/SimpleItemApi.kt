package me.owdding.skyocean.api

import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId.Companion.ATTRIBUTE
import me.owdding.skyocean.api.SkyOceanItemId.Companion.ENCHANTMENT
import me.owdding.skyocean.api.SkyOceanItemId.Companion.ITEM
import me.owdding.skyocean.api.SkyOceanItemId.Companion.PET
import me.owdding.skyocean.api.SkyOceanItemId.Companion.RUNE
import me.owdding.skyocean.api.SkyOceanItemId.Companion.UNKNOWN
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils.ItemBuilder
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
object SimpleItemApi {

    private val cache: MutableMap<SkyOceanItemId, ItemStack> = mutableMapOf()
    private val nameCache: MutableMap<String, SkyOceanItemId> = mutableMapOf()

    init {
        val start = currentInstant()
        RepoAPI.pets().pets().entries.associate { (id, data) -> data.name to SkyOceanItemId.pet(id) }
            .let(nameCache::putAll)

        RepoAPI.runes().runes().entries.flatMap { (id, data) ->
            data.map { rune ->
                rune.name.stripColor() to SkyOceanItemId.rune("$id:${rune.tier}")
            }
        }.toMap().let(nameCache::putAll)

        RepoAPI.enchantments().enchantments().flatMap { (id, enchantments) ->
            enchantments.levels.map { (level, enchantment) ->
                "${enchantments.name} ${enchantment.literalLevel}" to SkyOceanItemId.enchantment("$id:${enchantment.level}")
            }
        }.toMap().let(nameCache::putAll)

        RepoAPI.attributes().attributes().flatMap { (id, attribute) ->
            listOf(
                attribute.name to SkyOceanItemId.attribute(attribute.attributeId),
                attribute.shardName() to SkyOceanItemId.attribute(attribute.attributeId),
            )
        }.toMap().let(nameCache::putAll)

        RepoAPI.items().items().entries.mapNotNull { (id, element) ->
            val components =
                element.getPath("['components'].['minecraft:custom_name'].['text']") ?: return@mapNotNull null
            components.asString.stripColor() to SkyOceanItemId.item(id)
        }.toMap().let(nameCache::putAll)

        val newCache = nameCache.mapKeys { (key) -> key.lowercase().stripColor() }
        nameCache.clear()
        nameCache.putAll(newCache)
        SkyOcean.trace("Cached ${nameCache.size} item names in ${start.since().toReadableTime(allowMs = true)}")
    }

    fun findIdByName(name: String) = nameCache[name.lowercase().stripColor()]

    fun getItemById(id: SkyOceanItemId): ItemStack = cache.getOrPut(id) {
        val itemId = id.id.removePrefix(ITEM)

        if (itemId == UNKNOWN) {
            return@getOrPut ItemBuilder(Items.BARRIER) {
                name("Unknown Item")
            }
        }

        RepoItemsAPI.getItem(itemId)
    }

    fun getPetById(id: SkyOceanItemId): ItemStack = cache.getOrPut(id) {
        val petId = id.id.removePrefix(PET)

        if (petId == UNKNOWN) {
            return@getOrPut ItemBuilder(Items.BARRIER) {
                name("Unknown Pet")
            }
        }

        if (petId.contains(":")) {
            val (petId, rarity) = petId.split(":")
            val sbRarity = runCatching { SkyBlockRarity.valueOf(rarity) }.getOrNull()
            val pet = sbRarity?.let { RepoPetsAPI.getPetAsItemOrNull(PetQuery(petId, it, 1)) }
            pet?.let { return@getOrPut it }
        }

        return@getOrPut SkyBlockRarity.entries.reversed().firstNotNullOfOrNull { skyBlockRarity ->
            RepoPetsAPI.getPetAsItemOrNull(PetQuery(petId.substringBefore(":"), skyBlockRarity, 1))
        } ?: Items.BARRIER.defaultInstance
    }

    fun getRuneById(id: SkyOceanItemId): ItemStack = cache.getOrPut(id) {
        val runeId = id.id.removePrefix(RUNE)

        if (runeId == UNKNOWN) {
            return@getOrPut ItemBuilder(Items.BARRIER) {
                name("Unknown Rune")
            }
        }

        if (runeId.contains(":")) {
            val (runeId, literalLevel) = runeId.split(":")
            val level = literalLevel.toIntValue()
            RepoRunesAPI.getRuneAsItemOrNull(runeId, level)?.let { return@getOrPut it }
        }

        for (i in 3 downTo 0) {
            RepoRunesAPI.getRuneAsItemOrNull(runeId.substringBefore(":"), i)?.let { return@getOrPut it }
        }

        return@getOrPut ItemBuilder(Items.BARRIER) {
            name("Unknown Rune: $runeId")
        }
    }

    fun getEnchantmentById(id: SkyOceanItemId): ItemStack = cache.getOrPut(id) {
        val enchantmentId = id.id.removePrefix(ENCHANTMENT)

        if (enchantmentId == UNKNOWN) {
            return@getOrPut ItemBuilder(Items.BARRIER) {
                name("Unknown Enchantment")
            }
        }

        if (enchantmentId.contains(":")) {
            val (enchantmentId, literalLevel) = enchantmentId.split(":")
            val level = literalLevel.toIntValue()
            EnchantmentApi.getEnchantmentAsItemOrNull(enchantmentId, level)?.let { return@getOrPut it }
        }

        for (i in 10 downTo 0) {
            EnchantmentApi.getEnchantmentAsItemOrNull(enchantmentId.substringBefore(":"), i)?.let { return@getOrPut it }
        }

        return@getOrPut ItemBuilder(Items.BARRIER) {
            name("Unknown Enchantment: $enchantmentId")
        }
    }

    fun getAttributeById(id: SkyOceanItemId): ItemStack = cache.getOrPut(id) {
        val attributeId = id.id.removePrefix(ATTRIBUTE)

        if (attributeId == UNKNOWN) {
            return@getOrPut ItemBuilder(Items.BARRIER) {
                name("Unknown Attribute")
            }
        }

        AttributeApi.getAttributeByIdOrNull(attributeId)?.let { return@getOrPut it }

        return@getOrPut ItemBuilder(Items.BARRIER) {
            name("Unknown Attribute: $attributeId")
        }
    }

}
