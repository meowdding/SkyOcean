package me.owdding.skyocean.data.profile

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils.containerItems
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId

@Module
object SackOfSacksItemStorage {

    private val storage = SkyOcean.profileStorage(
        fileName = "sack_of_sacks",
        defaultData = { mutableListOf() },
        CodecHelpers.mutableList<ItemStack>(),
    )

    val items: List<ItemStack> get() = storage.get().orEmpty()

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
