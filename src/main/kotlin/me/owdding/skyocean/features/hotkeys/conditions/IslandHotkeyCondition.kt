package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent

@GenerateCodec
data class IslandHotkeyCondition(
    @Compact val islands: MutableSet<SkyBlockIsland> = mutableSetOf(),
) : SelectHotkeyCondition<SkyBlockIsland> {
    override val text: String = "Islands"
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.IslandHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.ISLAND

    override fun data(): MutableSet<SkyBlockIsland> = islands
    override fun possibilities(): List<SkyBlockIsland> = SkyBlockIsland.entries
    override fun nameConverter(data: SkyBlockIsland): Component = data.name.toTitleCase().asComponent()

    override fun test(): Boolean = SkyBlockIsland.inAnyIsland(islands)
}
