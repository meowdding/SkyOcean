@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package me.owdding.skyocean.features.inventory.accessories

import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.getRealRarity
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity.*
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import java.util.*

@LateInitModule
object AccessoriesAPI {

    internal var families: Map<String, AccessoryFamily> = emptyMap()
    internal var ignored: Set<SkyBlockId> = emptySet()
    internal var rarityUpgraded: Map<SkyBlockId, AccessoryRarityUpgraded> = emptyMap()
    internal var disallowedOriginFamilies: Set<String> = emptySet()

    private val HEGEMONY = SkyBlockId.item("hegemony_artifact")

    fun getFamily(id: SkyBlockId): AccessoryFamily? {
        return families.values.find { it.contains(id) }
    }

    fun AccessoryFamily.isDisallowed(): Boolean = isDisallowedOriginFamily(family)

    private fun getMpFromRarity(rarity: SkyBlockRarity): Int {
        return when (rarity) {
            COMMON -> 3
            UNCOMMON -> 5
            RARE -> 8
            EPIC -> 12
            LEGENDARY -> 16
            MYTHIC -> 22
            SPECIAL -> 3
            VERY_SPECIAL -> 5
            else -> 1
        }
    }

    fun getMp(item: ItemStack): Int {
        val realRarity = item.getRealRarity() ?: return 1
        var mp = getMpFromRarity(realRarity)
        if (item.getSkyBlockId() == HEGEMONY) mp *= 2 // hegemony gives double mp
        return mp
    }

    private fun calculateIsDisallowedOrigin(family: AccessoryFamily): Boolean {
        return family.flatMapItems().any { id ->
            val data = ItemData.getItemData(id.skyblockId) ?: return@any false
            when(data.origin) {
                BINGO -> true
                RIFT -> !data.riftTransferable
                else -> false
            }
        }
    }

    fun getRarityUpgraded(id: SkyBlockId): AccessoryRarityUpgraded? = rarityUpgraded[id]
    fun isIgnored(id: SkyBlockId): Boolean = id in ignored
    fun upgradesRarity(id: SkyBlockId): Boolean = id in rarityUpgraded
    fun isDisallowedOriginFamily(family: String) = family in disallowedOriginFamilies
    fun isDisallowedOrigin(id: SkyBlockId): Boolean = getFamily(id)?.isDisallowed() == true

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepo() {
        families = emptyMap()
        ignored = emptySet()
        rarityUpgraded = emptyMap()


        families = Utils.loadRemoteRepoData<AccessoryFamily, List<AccessoryFamily>>("accessories/families", CodecUtils::list)
            ?.associateBy { it.family }
            .orEmpty()
            .onEach { (_, family) ->
                // Initialize the tier of the tiers
                family.forEachIndexed { index, tier ->
                    tier.tier = index
                }
            }

        val emptyFamilies = families.filter { it.value.isEmpty() }.keys
        if (emptyFamilies.isNotEmpty()) {
            SkyOcean.error("Accessory families are empty: $emptyFamilies")
        }

        ignored = Utils.loadRemoteRepoData<SkyBlockId, Set<SkyBlockId>>("accessories/ignored_accessories", CodecUtils::set).orEmpty()
        rarityUpgraded = Utils.loadRemoteRepoData<AccessoryRarityUpgraded, List<AccessoryRarityUpgraded>>("accessories/rarity_upgraded", CodecUtils::list)
            ?.associateBy { it.item }.orEmpty()

        disallowedOriginFamilies = families.filterValues(::calculateIsDisallowedOrigin).keys
    }

    @Subscription
    fun onRegisterSkyOceanCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("accessories") {
            then("copy") {
                fun <T : Any> copy(name: String, data: () -> T, codec: Codec<T>) {
                    thenCallback(name) {
                        McClient.clipboard = data().toJson(codec).toPrettyString()
                        Text.of("Copied accessories ${name.toTitleCase()} data to clipboard!").sendWithPrefix()
                    }
                }

                copy("families", ::families, CodecUtils.map(Codec.STRING, AccessoryFamily.CODEC).unsafeCast())
                copy("ignored", ::ignored, CodecUtils.set(SkyBlockId.CODEC))
                copy("rarity_upgraded", ::rarityUpgraded, CodecUtils.map(SkyBlockId.CODEC, AccessoryRarityUpgraded.CODEC).unsafeCast())
                copy("disallowed_origin_families", ::disallowedOriginFamilies, CodecUtils.set(Codec.STRING))
            }

            then("check") {
                thenCallback("missing") { Scheduling.async(::checkMissing) }
                thenCallback("unknown") { Scheduling.async(::checkUnknown) }
            }
        }
    }

    // Creates every single item in skyblock and gets the accessories that don't have a family or are ignored
    private fun checkMissing() {
        val allAccessories = RepoAPI.items().items().map { json ->
            ItemStack.CODEC.parse(JsonOps.INSTANCE, json.value).orThrow
        }.filterTo(mutableSetOf()) {
            val category = it.getData(DataTypes.CATEGORY) ?: return@filterTo false
            category.equalsAny(SkyBlockCategory.ACCESSORY, SkyBlockCategory.HATCESSORY, ignoreDungeon = true)
        }.mapNotNullTo(mutableSetOf()) { it.getSkyBlockId() }

        val storedAccessories: Set<SkyBlockId> = buildSet {
            families.values.forEach { family ->
                family.tiers.forEach(::addAll)
            }
            addAll(ignored)
        }

        allAccessories.removeAll(storedAccessories)

        if (allAccessories.isEmpty()) {
            text("All accessories have families!").sendWithPrefix()
        } else {
            text("Not all accessories have families! Click to copy missing ones") {
                this.color = OceanColors.WARNING
                this.hover = text("Click to copy missing ones!")
                onClick {
                    val codec = CodecUtils.set(SkyBlockId.CODEC)
                    McClient.clipboard = allAccessories.toJsonOrThrow(codec).toPrettyString()
                    text("Copied missing accessories to clipboard!").sendWithPrefix()
                }
            }.sendWithPrefix()
        }
    }

    private fun checkUnknown() {
        val knownAccessories: Set<SkyBlockId> = buildSet {
            families.values.forEach { family ->
                family.tiers.forEach(::addAll)
            }
            addAll(ignored)
        }

        val unknownAccessories = knownAccessories.filterTo(mutableSetOf()) {
            it.toItem().`is`(Items.BARRIER)
        }

        if (unknownAccessories.isEmpty()) {
            text("No unknown accessories found!").sendWithPrefix()
        } else {
            text("Found unknown accessories! Click to copy them") {
                this.color = OceanColors.WARNING
                this.hover = text("Click to copy unknown ones!")
                onClick {
                    val codec = CodecUtils.set(SkyBlockId.CODEC)
                    McClient.clipboard = unknownAccessories.toJsonOrThrow(codec).toPrettyString()
                    text("Copied unknown accessories to clipboard!").sendWithPrefix()
                }
            }.sendWithPrefix()
        }
    }

}

@GenerateCodec
data class AccessoryFamily(
    val family: String,
    @Compact val tiers: List<AccessoryTier>,
) : List<AccessoryTier> by tiers {
    //region Functions
    inline val maxTier: Int get() = lastIndex
    fun flatMapItems(): Sequence<SkyBlockId> = asSequence().flatMap(AccessoryTier::items)
    operator fun get(id: SkyBlockId): AccessoryTier? = find { id in it }
    fun contains(id: SkyBlockId): Boolean = any { id in it }
    //endregion

    companion object {
        val CODEC: Codec<AccessoryFamily> = SkyOceanCodecs.getCodec()
    }
}

// Not a data class so that equality checks aren't done with its contents
class AccessoryTier(
    val items: Set<SkyBlockId>,
) : Set<SkyBlockId> by items, Comparable<AccessoryTier> {
    /** Index of the [AccessoryTier] in its family, gets initialized when family is created */
    var tier: Int = -1
        internal set

    override fun compareTo(other: AccessoryTier): Int = tier.compareTo(other.tier)

    companion object {
        @IncludedCodec
        val CODEC: Codec<AccessoryTier> = CodecUtils.compactSet(SkyBlockId.CODEC).xmap(::AccessoryTier, AccessoryTier::items)
    }
}

@GenerateCodec
data class AccessoryRarityUpgraded(
    val item: SkyBlockId,
    val rarities: EnumSet<SkyBlockRarity>,
) : Set<SkyBlockRarity> by rarities {
    fun isMax(rarity: SkyBlockRarity) = rarities.maxOrNull() == rarity
    fun nextAfter(rarity: SkyBlockRarity): SkyBlockRarity? = firstOrNull { it > rarity }
    companion object {
        val CODEC: Codec<AccessoryRarityUpgraded> = RecordCodecBuilder.create {
            it.group(
                SkyOceanCodecs.getCodec<SkyBlockId>().fieldOf("item").forGetter(AccessoryRarityUpgraded::item),
                CodecUtils.enumSet(SkyOceanCodecs.getCodec<SkyBlockRarity>()).fieldOf("rarities").forGetter(AccessoryRarityUpgraded::rarities),
            ).apply(it, ::AccessoryRarityUpgraded)
        }
    }
}
