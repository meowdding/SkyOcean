package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.config.utils.GenericDropdown.Companion.blockDropdown
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.EntityTypes
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.list
import net.minecraft.client.renderer.block.BlockModelResolver
import net.minecraft.client.renderer.block.model.BlockDisplayContext
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.golem.SnowGolem
import net.minecraft.world.level.block.Blocks

@RegisterAnimalModifier
object SnowGolemModifier : AnimalModifier<SnowGolem, SnowGolemRenderState> {
    override val type: EntityType<SnowGolem> = EntityTypes.SNOW_GOLEM

    var blockSelector = PlayerAnimalConfig.createEntry("snow_golem_block") { id, type ->
        blockDropdown(
            id = id,
            default = Blocks.AIR,
            options = Registries.BLOCK.list(),
        ) {
            this.translation = createTranslationKey("snow_golem", "${type}_block")
            condition = isSelected(EntityTypes.SNOW_GOLEM)
        }
    }


    override fun apply(
        resolver: BlockModelResolver,
        avatarState: AvatarRenderState,
        state: SnowGolemRenderState,
        partialTicks: Float,
    ) {
        resolver.update(state.headBlock, blockSelector.select(avatarState).defaultBlockState(), BlockDisplayContext.create())
    }
}
