package me.owdding.skyocean.features.inventory.accessories

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.inventory.accessories.AccessoriesAPI.isDisallowed
import me.owdding.skyocean.features.inventory.accessories.AccessoriesHelper.AccessoryResult.*
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.getRealRarity
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.Icons
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.items.accessory.AccessoryBagAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.seconds

@Module
@ItemModifier
object AccessoriesHelper : AbstractItemModifier() {

    fun getCurrentAccessories(): MutableList<SkyBlockId> = AccessoryBagAPI.getItems().mapNotNullTo(mutableListOf()) { it.item.getSkyBlockId() }

    fun getFamilyAndTier(id: SkyBlockId): Pair<AccessoryFamily, AccessoryTier>? {
        val family = AccessoriesAPI.getFamily(id) ?: return null
        val tier = family[id] ?: return null
        return family to tier
    }

    // Returns highest tier in family, or -1 if no tier has been collected
    fun highestTierCollectedInFamily(id: SkyBlockId): AccessoryTier? {
        val current = currentIds
        val (family, _) = getFamilyAndTier(id) ?: return null
        return family.lastOrNull { it.any(current::contains) }
    }

    fun hasDuplicate(id: SkyBlockId): Boolean {
        val current = getCurrentAccessories()
        val (_, tier) = getFamilyAndTier(id) ?: return false
        current.remove(id)
        return tier.any(current::contains)
    }

    fun ignoreDuplicate(id: SkyBlockId): Boolean = getFamilyAndTier(id)?.first?.ignoreDuplicates ?: false

    fun getResult(id: SkyBlockId): AccessoryResult {
        val (family, tier) = getFamilyAndTier(id) ?: return NONE
        val hasId = id in currentIds
        // If you have another accessory of the same line, in the same tier
        if (hasId && hasDuplicate(id) && !ignoreDuplicate(id)) return DUPLICATE

        // If you don't have any accessory in this family, this returns null
        val highestTierInFamily = highestTierCollectedInFamily(id) ?: return MISSING

        // If you have the accessory, AND it's the max tier of the family
        if (tier == family.last() && hasId) return MAXED
        // If you own a lower tier of this family
        if (tier > highestTierInFamily) return UPGRADE
        // If this accessory is a lower tier than one you already own
        if (tier < highestTierInFamily) return DOWNGRADE

        // If own this accessory and it can be upgraded
        if (hasId) return UPGRADEABLE

        return NONE // I don't think this should be reachable?
    }

    enum class AccessoryResult(val component: Component?, val color: Int = 0) {
        // You own that accessory and it's the max tier
        MAXED(Icons.CHECKMARK, TextColor.GREEN),

        // You own a lower tier of this accessory
        UPGRADE("✦", TextColor.BLUE),

        // You own this accessory, and it can be upgraded
        UPGRADEABLE("▲", TextColor.YELLOW),

        // You don't own any accessories of this family
        MISSING(Icons.CROSS, TextColor.RED),

        // You own multiple accessories of the same tier on the same family
        DUPLICATE("☰", TextColor.DARK_PURPLE),

        // You own a higher tier of this accessory
        DOWNGRADE("▼", TextColor.GRAY),

        NONE(null),
        ;

        constructor(icon: String, color: Int) : this(Text.of(icon, color), color)
    }

    val currentIds: Set<SkyBlockId> by CachedValue(1.seconds) { getCurrentAccessories().toSet() }

    fun getMissingAccessories(): Set<AccessoryFamily> {
        val ids = currentIds
        return AccessoriesAPI.families.values.filterTo(mutableSetOf()) { family ->
            family.flatMapItems().none { it in ids } && !family.isDisallowed()
        }
    }

    data class AccessoryRarityUpgradeData(
        val family: AccessoryFamily,
        val currentItem: ItemStack,
        val nextRarity: SkyBlockRarity,
    )

    fun getUpgradeableRarityAccessories(): List<AccessoryRarityUpgradeData> {
        return AccessoryBagAPI.getItems()
            .asSequence()
            .map { it.item }
            .mapNotNull { item ->
                val id = item.getSkyBlockId() ?: return@mapNotNull null
                val upgraded = AccessoriesAPI.getRarityUpgraded(id) ?: return@mapNotNull null
                val family = AccessoriesAPI.getFamily(id) ?: return@mapNotNull null
                if (family.isDisallowed()) return@mapNotNull null
                val realRarity = item.getRealRarity() ?: return@mapNotNull null
                val next = upgraded.nextAfter(realRarity) ?: return@mapNotNull null
                AccessoryRarityUpgradeData(family, item, next)
            }.toList()
    }

    data class AccessoryUpgradeData(
        val currentItem: ItemStack,
        val family: AccessoryFamily,
        val nextTier: AccessoryTier,
        val nextTierInt: Int,
    )

    fun getUpgradeableAccessories(): List<AccessoryUpgradeData> {
        return AccessoryBagAPI.getItems()
            .asSequence()
            .map { it.item }
            .mapNotNull { item ->
                val id = item.getSkyBlockId() ?: return@mapNotNull null
                val family = AccessoriesAPI.getFamily(id) ?: return@mapNotNull null
                if (family.isDisallowed()) return@mapNotNull null
                val tier = family.indexOfFirst { id in it } + 1
                val nextTier = family.getOrNull(tier) ?: return@mapNotNull null
                AccessoryUpgradeData(item, family, nextTier, tier)
            }
            .sortedByDescending { it.nextTierInt } // we sort by the tier to make sure we always use the highest tier of each family
            .distinctBy { it.family }
            .toList()
    }

    @Subscription
    fun onRegisterSkyOceanCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("accessories") {
            thenCallback("screen") {
                if (!LocationAPI.isOnSkyBlock) {
                    Text.of("You must be on SkyBlock!") { this.color = TextColor.RED }.sendWithPrefix()
                    return@thenCallback
                }
                if (SkyBlockIsland.THE_RIFT.inIsland()) {
                    Text.of("You can't use Accessories Helper in the Rift!", TextColor.RED).sendWithPrefix()
                    return@thenCallback
                }
                McClient.setScreen(AccessoriesHelperScreen)
            }
        }
    }

    override val displayName: Component
        get() = Text.of("Accessories Helper")
    override val isEnabled: Boolean
        get() = true

    override fun appliesTo(itemStack: ItemStack): Boolean {
        if (itemStack.isEmpty) return false
        val category = itemStack[DataTypes.CATEGORY] ?: return false
        if (!category.equalsAny(SkyBlockCategory.ACCESSORY, SkyBlockCategory.HATCESSORY)) return false
        val id = itemStack.getSkyBlockId() ?: return false
        return getResult(id) != NONE
    }

    override fun itemCountOverride(itemStack: ItemStack): Component? {
        val id = itemStack.getSkyBlockId() ?: return null
        return getResult(id).component
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result = withMerger(list) {
        val result = item.getSkyBlockId()?.let(::getResult) ?: return@withMerger null
        if (result.component == null) return@withMerger null
        copy()
        
        add(
            Text.join(
                result.component,
                " ",
                Text.of(result.name, result.color),
            ),
        )
        addRemaining()
        Result.modified
    }

}
