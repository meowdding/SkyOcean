package me.owdding.skyocean.features.dungeons.gambling.chest

import com.mojang.serialization.Codec
import com.teamresourceful.resourcefullib.common.collections.WeightedCollection
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@GenerateCodec
data class DungeonItem(val id: SkyBlockId, val weight: Double) {

    val item by lazy { this.id.toItem() }
}

class DungeonChestItems(val items: List<DungeonItem>) {

    private val lookup: Map<SkyBlockId, DungeonItem> = items.associateBy { it.id }
    private val weights: WeightedCollection<DungeonItem> = WeightedCollection.of(items) { it.weight }

    operator fun get(id: SkyBlockId): DungeonItem? = lookup[id]
    fun getRandomItem(): DungeonItem? = if (weights.isEmpty()) null else weights.next()

}

@LateInitModule
object DungeonItems {

    private val items = Utils.loadRepoData<Map<DungeonFloor, Map<DungeonChestType, DungeonChestItems>>>("dungeon_chests") {
        Codec.unboundedMap(
            SkyOceanCodecs.getCodec<DungeonFloor>(),
            Codec.unboundedMap(
                SkyOceanCodecs.getCodec<DungeonChestType>(),
                SkyOceanCodecs.getCodec<DungeonItem>().listOf().xmap(::DungeonChestItems, DungeonChestItems::items)
            )
        )
    }

    operator fun get(floor: DungeonFloor, type: DungeonChestType): DungeonChestItems? {
        return items[floor]?.get(type)
    }
}
