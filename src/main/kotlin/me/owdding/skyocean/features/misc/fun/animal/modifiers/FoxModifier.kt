package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.FoxRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.fox.Fox
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import kotlin.time.Duration.Companion.milliseconds

@RegisterAnimalModifier
object FoxModifier : AnimalModifier<Fox, FoxRenderState> {
    override val type: EntityType<Fox> = EntityType.FOX
    private val foxVariants = Fox.Variant.entries

    var foxVariant = PlayerAnimalConfig.createEntry("fox_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("fox", "${type}_variant")
            condition = isSelected(EntityType.FOX)
        }
    }

    var shouldSleep = PlayerAnimalConfig.createEntry("fox_eepy") { id, type ->
        boolean(id, false) {
            this.translation = createTranslationKey("fox", "${type}_eepy")
            condition = isSelected(EntityType.FOX)
        }
    }

    var sleepDelay = PlayerAnimalConfig.createEntry("fox_sleep_delay") { id, type ->
        double(id, 5.0) {
            this.translation = createTranslationKey("fox", "${type}_sleep_delay")
            condition = isSelected(EntityType.FOX)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: FoxRenderState,
        partialTicks: Float,
    ) {
        state.variant = foxVariant.select(avatarState).variant ?: getRandom(avatarState, foxVariants)
        state.isSitting = avatarState.isCrouching

        if (shouldSleep.select(avatarState)) {
            val delay = sleepDelay.select(avatarState) * 1000
            state.isSleeping = AvatarRenderStateAccessor.getLastMoveTime(avatarState)?.let { it + delay.milliseconds < currentInstant() } == true
        }
    }

    enum class Variant(val variant: Fox.Variant?) : Translatable {
        RANDOM(null),
        RED(Fox.Variant.RED),
        SNOW(Fox.Variant.SNOW),
        ;

        override fun getTranslationKey(): String = createTranslationKey("fox", "variant", name)
    }
}
