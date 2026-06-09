package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.InventoryTitle
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import java.util.concurrent.CopyOnWriteArrayList

@Module
object MinionStorage {

    private val storage = ProfileStorage(
        0,
        { CopyOnWriteArrayList() },
        "minions",
        {
            CodecHelpers.copyOnWriteList(SkyOceanCodecs.getCodec<Minion>())
        },
    )

    val unlockedMinions: CopyOnWriteArrayList<Minion> get() = storage.get() ?: CopyOnWriteArrayList()

    fun getMinionTier(id: String) = unlockedMinions.find { it.partId == id }?.maxUnlock

    @Subscription(priority = Subscription.HIGHEST)
    @InventoryTitle("Crafted Minions")
    fun onOpen(event: ContainerInitializedEvent) {
        val currentMinions = storage.get() ?: CopyOnWriteArrayList()
        var dataChanged = false

        for (item in event.containerItems) {
            if (item.isEmpty) continue

            val partId = item.cleanName.uppercase().substringBeforeLast(" ").replace(" ", "_")

            if (item.`is`(Items.PLAYER_HEAD)) {
                val maxTier = item.getRawLore().count { it.contains("✔") }

                if (maxTier > 0) {
                    val existing = currentMinions.find { it.partId == partId }
                    if (existing == null) {
                        currentMinions.add(Minion(partId, maxTier))
                        dataChanged = true
                    } else if (maxTier > existing.maxUnlock) {
                        currentMinions.remove(existing)
                        currentMinions.add(Minion(partId, maxTier))
                        dataChanged = true
                    }
                }
            } else if (item.`is`(Items.GRAY_DYE)) {
                val existing = currentMinions.find { it.partId == partId }
                if (existing == null) {
                    currentMinions.add(Minion(partId, 0))
                    dataChanged = true
                }
            }
        }

        if (dataChanged) {
            storage.set(currentMinions)
            storage.save()
        }
    }

    @GenerateCodec
    data class Minion(val partId: String, val maxUnlock: Int)
}
