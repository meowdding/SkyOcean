package me.owdding.skyocean.features.hotkeys.conditions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.garden.PlotAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent

@GenerateCodec
data class GardenPlotHotkeyCondition(@Compact val plots: MutableSet<PlotId> = mutableSetOf()) : SelectHotkeyCondition<GardenPlotHotkeyCondition.PlotId> {
    override val codec: MapCodec<out HotkeyCondition> = SkyOceanCodecs.getMapCodec<GardenPlotHotkeyCondition>()
    override val type: HotkeyConditionType get() = HotkeyConditionType.GARDEN_PLOT
    override val text: String get() = "Garden Plot"

    override fun test() = plots.any { it.isInPlot() }
    override fun data(): MutableSet<PlotId> = plots

    override fun possibilities(): List<PlotId> = PlotId.entries

    override fun nameConverter(data: PlotId): Component = data.name.toTitleCase().asComponent()


    enum class PlotId {
        BARN,
        PLOT_1,
        PLOT_2,
        PLOT_3,
        PLOT_4,
        PLOT_5,
        PLOT_6,
        PLOT_7,
        PLOT_8,
        PLOT_9,
        PLOT_10,
        PLOT_11,
        PLOT_12,
        PLOT_13,
        PLOT_14,
        PLOT_15,
        PLOT_16,
        PLOT_17,
        PLOT_18,
        PLOT_19,
        PLOT_20,
        PLOT_21,
        PLOT_22,
        PLOT_23,
        PLOT_24,
        ;

        fun isInPlot() = PlotAPI.getCurrentPlot()?.id == ordinal
    }

    override fun duplicate(): HotkeyCondition = copy(plots = plots.toMutableSet())
}
