package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonClass
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent

@GenerateCodec
data class DungeonFloorHotkeyCondition(
    @Compact val floors: MutableSet<DungeonFloor> = mutableSetOf(),
) : SelectHotkeyCondition<DungeonFloor> {
    override val text: String = "Dungeon Floors"
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.DungeonFloorHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.DUNGEON_FLOOR

    override fun data(): MutableSet<DungeonFloor> = floors
    override fun possibilities(): List<DungeonFloor> = DungeonFloor.entries
    override fun nameConverter(data: DungeonFloor): Component = data.longName.replace("The Catacombs ", "").asComponent()

    override fun test(): Boolean = DungeonAPI.dungeonFloor in floors
}

@GenerateCodec
data class DungeonClassHotkeyCondition(
    @Compact val classes: MutableSet<DungeonClass> = mutableSetOf(),
) : SelectHotkeyCondition<DungeonClass> {
    override val text: String = "Dungeon Classes"
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.DungeonClassHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.DUNGEON_CLASS

    override fun data(): MutableSet<DungeonClass> = classes
    override fun possibilities(): List<DungeonClass> = DungeonClass.entries
    override fun nameConverter(data: DungeonClass): Component = data.displayName.asComponent()

    override fun test(): Boolean = DungeonAPI.dungeonClass in classes
}
