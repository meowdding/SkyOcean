package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.EntityTypes
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.block.BlockModelResolver
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.FoxRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.fox.Fox
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import kotlin.time.Duration.Companion.milliseconds

@RegisterAnimalModifier
object FoxModifier : AnimalModifier<Fox, FoxRenderState> {
    override val type: EntityType<Fox> = EntityTypes.FOX
    private val foxVariants = Fox.Variant.entries

    var foxVariant = PlayerAnimalConfig.createEntry("fox_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("fox", "${type}_variant")
            condition = isSelected(EntityTypes.FOX)
        }
    }

    var shouldSleep = PlayerAnimalConfig.createEntry("fox_eepy") { id, type ->
        boolean(id, false) {
            this.translation = createTranslationKey("fox", "${type}_eepy")
            condition = isSelected(EntityTypes.FOX)
        }
    }

    var sleepDelay = PlayerAnimalConfig.createEntry("fox_sleep_delay") { id, type ->
        double(id, 5.0) {
            this.translation = createTranslationKey("fox", "${type}_sleep_delay")
            condition = isSelected(EntityTypes.FOX)
        }
    }

    override fun apply(
        resolver: BlockModelResolver,
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
