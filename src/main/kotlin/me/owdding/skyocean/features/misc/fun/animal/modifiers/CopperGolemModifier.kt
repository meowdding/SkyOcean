package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.config.utils.GenericDropdown.Companion.blockDropdown
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.golem.CopperGolem
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.WeatheringCopper
import java.util.*

@RegisterAnimalModifier
object CopperGolemModifier : AnimalModifier<CopperGolem, CopperGolemRenderState> {
    override val type: EntityType<CopperGolem> = EntityType.COPPER_GOLEM
    val states = WeatheringCopper.WeatherState.entries

    var copperState = PlayerAnimalConfig.createEntry("copper_state") { id, type ->
        enum(id, WeatherState.RANDOM) {
            this.translation = "skyocean.config.misc.fun.player_animals.copper_golem.${type}_variant"
            condition = isSelected(EntityType.COPPER_GOLEM)
        }
    }

    var blockSelector = PlayerAnimalConfig.createEntry("copper_block") { id, type ->
        blockDropdown(
            id = id,
            default = Blocks.AIR,
            options = Registries.BLOCK.list(),
        ) {
            this.translation = "skyocean.config.misc.fun.player_animals.copper_golem.${type}_block"
            condition = isSelected(EntityType.COPPER_GOLEM)
        }
    }

    fun getWeatherState(state: AvatarRenderState): WeatheringCopper.WeatherState = copperState.select(state).state ?: getRandom(state, states)

    override fun apply(
        avatarState: AvatarRenderState,
        state: CopperGolemRenderState,
        partialTicks: Float,
    ) {
        state.weathering = getWeatherState(avatarState)
        val block = blockSelector.select(avatarState).takeUnless { it == Blocks.AIR }
        state.blockOnAntenna = Optional.ofNullable(block?.defaultBlockState())
    }

    enum class WeatherState(val state: WeatheringCopper.WeatherState?) : Translatable {
        RANDOM(null),

        UNAFFECTED(WeatheringCopper.WeatherState.UNAFFECTED),
        EXPOSED(WeatheringCopper.WeatherState.EXPOSED),
        WEATHERED(WeatheringCopper.WeatherState.WEATHERED),
        OXIDIZED(WeatheringCopper.WeatherState.OXIDIZED),
        ;

        override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_animals.copper_golem.state.${name.lowercase()}"
    }
}
