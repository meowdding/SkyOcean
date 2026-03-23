package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.config.utils.GenericDropdown.Companion.blockDropdown
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import net.minecraft.client.renderer.block.model.BlockDisplayContext
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState
import net.minecraft.client.renderer.item.properties.select.DisplayContext
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.golem.SnowGolem
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel

@RegisterAnimalModifier
object SnowGolemModifier : AnimalModifier<SnowGolem, SnowGolemRenderState> {
    override val type: EntityType<SnowGolem> = EntityType.SNOW_GOLEM

    //? >= 26.1 {
    var blockSelector = PlayerAnimalConfig.createEntry("snow_golem_block") { id, type ->
        blockDropdown(
            id = id,
            default = Blocks.AIR,
            options = Registries.BLOCK.list(),
        ) {
            this.translation = createTranslationKey("snow_golem", "${type}_block")
            condition = isSelected(EntityType.SNOW_GOLEM)
        }
    }
    //? } else {
    var snowGolemPumpkin = PlayerAnimalConfig.createEntry("snow_golem_pumpkin") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("snow_golem", "${type}_pumpkin")
            condition = isSelected(EntityType.SNOW_GOLEM)
        }
    }
    //? }


    override fun apply(
        avatarState: AvatarRenderState,
        state: SnowGolemRenderState,
        partialTicks: Float,
    ) {
        //? if >= 26.1 {
        McClient.self.blockModelResolver.update(state.headBlock, blockSelector.select(avatarState).defaultBlockState(), BlockDisplayContext.create())
        //? } else
        //state.hasPumpkin = snowGolemPumpkin.select(avatarState).select(avatarState)
    }
}
