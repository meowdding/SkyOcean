package me.owdding.skyocean.data.profile

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.Utils.containerItems
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

@Module
object SackOfSacksItemStorage {

    private val storage: ProfileStorage<MutableList<ItemStack>> = ProfileStorage(
        defaultData = { mutableListOf() },
        fileName = "sack_of_sacks",
        codec = { CodecHelpers.list() },
    )

    val items: List<ItemStack> get() = storage.get() ?: emptyList()

    @Subscription
    @OnlyOnSkyBlock
    @MustBeContainer
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!event.title.equals("sack of sacks", true)) return

        val sacks = event.inventory.containerItems().filterNot { it.getSkyBlockId() == null }
        storage.get()?.clear()
        storage.get()?.addAll(sacks)
        storage.save()
    }

}
