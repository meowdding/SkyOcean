@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package me.owdding.skyocean.features.inventory.accessories

import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import java.util.*

@LateInitModule
object AccessoriesAPI {

    internal var families: Map<String, AccessoryFamily> = emptyMap()
    internal var ignored: Set<SkyBlockId> = emptySet()
    internal var rarityUpgraded: Map<SkyBlockId, AccessoryRarityUpgraded> = emptyMap()

    fun getFamily(id: SkyBlockId): AccessoryFamily? {
        return families.values.find { it.contains(id) }
    }
    fun isIgnored(id: SkyBlockId): Boolean = id in ignored
    fun upgradesRarity(id: SkyBlockId): Boolean = id in rarityUpgraded

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepo() {
        families = emptyMap()
        ignored = emptySet()
        rarityUpgraded = emptyMap()


        families = Utils.loadRemoteRepoData<AccessoryFamily, List<AccessoryFamily>>("accessories/families", CodecUtils::list)
            ?.associateBy { it.family }.orEmpty()
        ignored = Utils.loadRemoteRepoData<SkyBlockId, Set<SkyBlockId>>("accessories/ignored_accessories", CodecUtils::set).orEmpty()
        rarityUpgraded = Utils.loadRemoteRepoData<AccessoryRarityUpgraded, List<AccessoryRarityUpgraded>>("accessories/rarity_upgraded", CodecUtils::list)
            ?.associateBy { it.item }.orEmpty()
    }

    @Subscription
    fun onRegisterSkyOceanCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDevWithCallback("accessories check_missing") {
            Scheduling.async(::checkMissing)
        }
    }

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

}

@GenerateCodec
data class AccessoryFamily(
    val family: String,
    @Compact val tiers: List<AccessoryTier>,
) : List<AccessoryTier> by tiers {
    //region Functions
    val maxTier: Int get() = lastIndex
    fun flatMapItems(): Sequence<SkyBlockId> = asSequence().flatMap(AccessoryTier::items)
    operator fun get(id: SkyBlockId): AccessoryTier? = find { id in it }
    fun contains(id: SkyBlockId): Boolean = any { id in it }
    //endregion
}

data class AccessoryTier(
    val items: Set<SkyBlockId>,
) : Set<SkyBlockId> by items {
    companion object {
        @IncludedCodec
        val CODEC: Codec<AccessoryTier> = CodecUtils.compactSet(SkyBlockId.CODEC).xmap(::AccessoryTier, AccessoryTier::items)
    }
}

@GenerateCodec
data class AccessoryRarityUpgraded(
    val item: SkyBlockId,
    val rarities: EnumSet<SkyBlockRarity>,
)
