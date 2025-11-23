package me.owdding.skyocean.features.inventory.accessories

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.CachedValue
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.inventory.accessories.AccessoriesHelper.AccessoryResult.NONE
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
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

    fun getFamilyAndTier(id: SkyBlockId): Pair<AccessoryFamily, Int>? {
        return AccessoriesAPI.families.values.firstNotNullOfOrNull { family ->
            val index = family.indexOfFirst { id in it }
            if (index == -1) return@firstNotNullOfOrNull null
            return@firstNotNullOfOrNull family to index
        }
    }

    // Returns highest tier in family, or -1 if no tier has been collected
    fun highestTierCollectedInFamily(id: SkyBlockId): Int {
        val current = currentIds
        val (family, _) = getFamilyAndTier(id) ?: return -1
        return family.withIndex().lastOrNull { it.value.any(current::contains) }?.index ?: -1
    }

    fun hasDuplicate(id: SkyBlockId): Boolean {
        val current = getCurrentAccessories()
        val (family, tier) = getFamilyAndTier(id) ?: return false
        current.remove(id)
        return family[tier].any(current::contains)
    }

    // temporary
    private val duplicateFamilies = setOf("cake_bag", "hatcessory", "personal_compactor", "personal_deletor")

    // TODO: use repo
    fun ignoreDuplicate(id: SkyBlockId): Boolean {
        val (family, _) = getFamilyAndTier(id) ?: return false
        return family.family in duplicateFamilies
    }

    fun getResult(id: SkyBlockId): AccessoryResult {
        val (family, tier) = getFamilyAndTier(id) ?: return NONE
        val hasId = id in currentIds
        // If you have another accessory of the same line, in the same tier
        if (hasId && hasDuplicate(id) && !ignoreDuplicate(id)) return DUPLICATE

        val highestTierInFamily = highestTierCollectedInFamily(id)
        // If you don't have any accessory in this family
        if (highestTierInFamily == -1) return MISSING
        // If you have the accessory, AND it's the max tier of the family
        if (tier == family.maxTier && hasId) return MAXED
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
        MAXED("✓", TextColor.GREEN),

        // You own a lower tier of this accessory
        UPGRADE("✦", TextColor.BLUE),

        // You own this accessory, and it can be upgraded
        UPGRADEABLE("▲", TextColor.YELLOW),

        // You don't own any accessories of this family
        MISSING("❌", TextColor.RED),

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
            family.flatMapItems().none { it in ids }
        }
    }

    data class AccessoryUpgradeData(
        val currentItem: ItemStack,
        val family: AccessoryFamily,
        val nextTier: AccessoryTier,
    )

    fun getUpgradeableAccessories(): List<AccessoryUpgradeData> {
        return AccessoryBagAPI.getItems()
            .asSequence()
            .map { it.item }
            .mapNotNull { item ->
                val id = item.getSkyBlockId() ?: return@mapNotNull null
                val family = AccessoriesAPI.getFamily(id) ?: return@mapNotNull null
                val tier = family.indexOfFirst { id in it }
                val nextTier = family.getOrNull(tier + 1) ?: return@mapNotNull null
                AccessoryUpgradeData(item, family, nextTier) to tier
            }
            .sortedByDescending { it.second } // we sort by the tier to make sure we always use the highest tier of each family
            .map { it.first }
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
        return !itemStack.isEmpty && itemStack[DataTypes.CATEGORY]
            ?.equalsAny(SkyBlockCategory.ACCESSORY, SkyBlockCategory.HATCESSORY, ignoreDungeon = true) == true
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
