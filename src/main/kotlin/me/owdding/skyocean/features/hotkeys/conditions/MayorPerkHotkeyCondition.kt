package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.data.MayorPerk
import tech.thatgravyboat.skyblockapi.api.data.MayorPerks
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent

@GenerateCodec
data class MayorPerkHotkeyCondition(
    @Compact val perks: MutableSet<MayorPerk> = mutableSetOf(),
) : SelectHotkeyCondition<MayorPerk> {
    override val text: String = "Mayor Perks"
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.MayorPerkHotkeyConditionCodec
    override val type: HotkeyConditionType = HotkeyConditionType.MAYOR_PERK

    override fun data(): MutableSet<MayorPerk> = perks
    override fun possibilities(): List<MayorPerk> = MayorPerks.perks.toList()
    override fun nameConverter(data: MayorPerk): Component = data.perkName.asComponent()

    override fun test(): Boolean = perks.any { it.active }
}
