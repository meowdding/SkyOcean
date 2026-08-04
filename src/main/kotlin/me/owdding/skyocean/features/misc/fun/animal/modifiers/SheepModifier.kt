package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalColor
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.EntityTypes
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.block.BlockModelResolver
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.SheepRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.sheep.Sheep
import net.minecraft.world.item.DyeColor

@RegisterAnimalModifier
object SheepModifier : AnimalModifier<Sheep, SheepRenderState> {
    override val type: EntityType<Sheep> = EntityTypes.SHEEP
    val colors = DyeColor.entries

    var sheepColor = PlayerAnimalConfig.createEntry("sheep_color") { id, type ->
        enum(id, AnimalColor.RANDOM) {
            this.translation = createTranslationKey("sheep", "${type}_color")
            condition = isSelected(EntityTypes.SHEEP)
        }
    }

    var isJebSheep = PlayerAnimalConfig.createEntry("sheep_jeb") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("sheep", "${type}_jeb")
            condition = isSelected(EntityTypes.SHEEP)
        }
    }

    var isSheared = PlayerAnimalConfig.createEntry("sheep_sheared") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("sheep", "${type}_sheared")
            condition = isSelected(EntityTypes.SHEEP)
        }
    }

    override fun apply(
        resolver: BlockModelResolver,
        avatarState: AvatarRenderState,
        state: SheepRenderState,
        partialTicks: Float,
    ) {
        state.woolColor = sheepColor.select(avatarState).dyeColor ?: getRandom(avatarState, colors)
        state.isJebSheep = isJebSheep.select(avatarState).select(avatarState)
        state.isSheared = isSheared.select(avatarState).select(avatarState)
    }
}
