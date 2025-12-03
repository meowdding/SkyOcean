package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.IronGolemRenderState
import net.minecraft.world.entity.Crackiness
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.IronGolem

@RegisterAnimalModifier
object IronGolemModifier : AnimalModifier<IronGolem, IronGolemRenderState> {
    override val type: EntityType<IronGolem> = EntityType.IRON_GOLEM
    val states = Crackiness.Level.entries

    var ironGolemState = PlayerAnimalConfig.createEntry("iron_golem_state") { id, type ->
        enum(id, State.RANDOM) {
            this.translation = createTranslationKey("iron_golem", "${type}_variant")
            condition = isSelected(EntityType.IRON_GOLEM)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: IronGolemRenderState,
        partialTicks: Float,
    ) {
        state.crackiness = ironGolemState.select(avatarState).level ?: getRandom(avatarState, states)
    }

    enum class State(val level: Crackiness.Level?) : Translatable {
        RANDOM(null),
        NONE(Crackiness.Level.NONE),
        LOW(Crackiness.Level.LOW),
        MEDIUM(Crackiness.Level.MEDIUM),
        HIGH(Crackiness.Level.HIGH),
        ;

        override fun getTranslationKey(): String = createTranslationKey("iron_golem", "variant", name)
    }
}
