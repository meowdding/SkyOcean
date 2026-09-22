@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package me.owdding.skyocean.features.inventory.accessories

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.*
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SimpleItemAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemOrigin.BINGO
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemOrigin.RIFT
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.extentions.capitalize
import tech.thatgravyboat.skyblockapi.utils.extentions.toSnakeCase
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.json.JsonArray
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import java.util.*
import kotlin.math.roundToInt
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

@Module
object AccessoriesAPI {

    internal var families: Map<String, AccessoryFamily> = emptyMap()
    internal var unobtainable: Set<SkyBlockId> = emptySet()
    internal var rarityUpgraded: Map<SkyBlockId, AccessoryRarityUpgraded> = emptyMap()
    internal var disallowedOriginFamilies: Set<String> = emptySet()
    internal var accessoryPower: AccessoryPowerRepoData? = null

    fun getFamily(id: SkyBlockId): AccessoryFamily? {
        return families.values.find { it.contains(id) }
    }

    fun AccessoryFamily.isDisallowed(): Boolean = isDisallowedOriginFamily(family)

    fun getAp(item: ItemStack): Int = accessoryPower?.getAccessoryPower(item) ?: 1

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
    fun isIgnored(id: SkyBlockId): Boolean = id in unobtainable
    fun upgradesRarity(id: SkyBlockId): Boolean = id in rarityUpgraded
    fun isDisallowedOriginFamily(family: String) = family in disallowedOriginFamilies
    fun isDisallowedOrigin(id: SkyBlockId): Boolean = getFamily(id)?.isDisallowed() == true

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepo() {
        families = emptyMap()
        unobtainable = emptySet()
        rarityUpgraded = emptyMap()
        accessoryPower = null


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

        unobtainable = Utils.loadRemoteRepoData<SkyBlockId, Set<SkyBlockId>>("accessories/unobtainable_accessories", CodecUtils::set).orEmpty()
        rarityUpgraded = Utils.loadRemoteRepoData<AccessoryRarityUpgraded, List<AccessoryRarityUpgraded>>("accessories/rarity_upgraded", CodecUtils::list)
            ?.associateBy { it.item }.orEmpty()
        accessoryPower = Utils.loadRemoteRepoData<AccessoryPowerRepoData>("accessories/magical_power")

        disallowedOriginFamilies = families.filterValues(::calculateIsDisallowedOrigin).keys
    }

    @Subscription
    fun onRegisterSkyOceanCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("accessories") {
            then("copy") {
                fun <T : Any> copy(prop: KProperty0<T>, codec: Codec<T>, name: String = prop.name) {
                    val snakeCase = name.toSnakeCase()
                    thenCallback(snakeCase) {
                        McClient.clipboard = prop.get().toJson(codec).toPrettyString()
                        val outputName = snakeCase.split("_").joinToString(separator = " ") { it.capitalize() }
                        Text.of("Copied accessories $outputName data to clipboard!").sendWithPrefix()
                    }
                }
                copy(::families, CodecUtils.map(Codec.STRING, AccessoryFamily.CODEC).unsafeCast())
                copy(::unobtainable, CodecUtils.set(SkyBlockId.CODEC), name = "ignored")
                copy(::rarityUpgraded, CodecUtils.map(SkyBlockId.CODEC, AccessoryRarityUpgraded.CODEC).unsafeCast())
                copy(::disallowedOriginFamilies, CodecUtils.set(Codec.STRING))
            }

            then("profile copy") {
                thenCallback("missing") {
                    val data = AccessoriesHelper.getMissingAccessories().map { it.family }
                    McClient.clipboard = data.toJson(CodecUtils.list(Codec.STRING)).toPrettyString()
                    text("Copied missing accessories in profile ${ProfileAPI.profileName} to clipboard!").sendWithPrefix()
                }
                thenCallback("upgradeable") {
                    McClient.clipboard = JsonArray {
                        AccessoriesHelper.getUpgradeableAccessories().forEach {
                            obj { obj ->
                                obj["family"] = it.family.family
                                obj["next_tier"] = it.nextTierInt
                            }
                        }
                    }.toPrettyString()
                    text("Copied upgradeable accessories in profile ${ProfileAPI.profileName} to clipboard!").sendWithPrefix()
                }
                thenCallback("upgradeable_rarity") {
                    McClient.clipboard = JsonArray {
                        AccessoriesHelper.getUpgradeableRarityAccessories().forEach {
                            obj { obj ->
                                obj["family"] = it.family.family
                                obj["next_rarity"] = it.nextRarity.name
                            }
                        }
                    }.toPrettyString()
                    text("Copied upgradeable rarity accessories in profile ${ProfileAPI.profileName} to clipboard!").sendWithPrefix()
                }
            }

            then("check") {
                thenCallback("missing") { Scheduling.async(::checkMissingAccessories) }
                thenCallback("unknown") { Scheduling.async(::checkUnknown) }
            }
        }
    }

    // Creates every single item in skyblock and gets the accessories that don't have a family or are ignored
    private fun checkMissingAccessories() {
        val allAccessories = SimpleItemAPI.getAllIds().filterTo(mutableSetOf()) {
            val category = it.toItem().getData(DataTypes.CATEGORY) ?: return@filterTo false
            category.equalsAny(SkyBlockCategory.ACCESSORY, SkyBlockCategory.HATCESSORY, ignoreDungeon = true)
        }
        val storedAccessories: Set<SkyBlockId> = buildSet {
            families.values.forEach { family ->
                family.tiers.forEach(::addAll)
            }
            addAll(unobtainable)
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
            addAll(unobtainable)
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
    @FieldName("ignore_duplicates") val ignoreDuplicates: Boolean = false,
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
        val CODEC: Codec<AccessoryRarityUpgraded> = SkyOceanCodecs.getCodec()
    }
}

@GenerateCodec
data class AccessoryPowerRepoData(
    val rarity: Map<SkyBlockRarity, Int>,
    val overrides: Map<SkyBlockId, Override>,
) {
    fun getAccessoryPower(item: ItemStack): Int {
        val override = overrides[item.getSkyBlockId()]
        val ap = rarity[item.getData(DataTypes.RARITY)] ?: 0

        return override?.apply(ap) ?: ap
    }
}

abstract class Override(val type: OverrideTypes) {
    abstract fun apply(num: Int): Int
}

@GenerateCodec
data class MultiplyOverride(val value: Double) : Override(OverrideTypes.MULTIPLY) {
    override fun apply(num: Int) = (num * value).roundToInt()
}

@GenerateDispatchCodec(Override::class)
enum class OverrideTypes(override val type: KClass<out Override>) : DispatchHelper<Override> {
    MULTIPLY(MultiplyOverride::class),
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
