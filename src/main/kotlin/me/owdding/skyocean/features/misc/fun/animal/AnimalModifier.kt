package me.owdding.skyocean.features.misc.`fun`.animal

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.ktmodules.AutoCollect
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import tech.thatgravyboat.skyblockapi.platform.texture

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@AutoCollect("AnimalModifiers")
annotation class RegisterAnimalModifier

private val dyeColors = DyeColor.entries

interface AnimalModifier<Type : LivingEntity, State : LivingEntityRenderState> {

    companion object {
        val AvatarRenderState.hash get() = AvatarRenderStateAccessor.getUUID(this)?.hashCode() ?: this.skin.texture.hashCode()
    }

    val type: EntityType<Type>
    fun <T> getRandom(state: AvatarRenderState, list: List<T>) = list[Math.floorMod(state.hash, list.size)]
    fun apply(avatarState: AvatarRenderState, state: State, partialTicks: Float)

    fun getCollarColor(state: AvatarRenderState): DyeColor? {
        val collarColor = PlayerAnimalConfig.collarColor.select(state)
        if (collarColor == CollarColor.NONE) return null
        return collarColor.dyeColor ?: getRandom(state, dyeColors)
    }

    enum class BooleanState(val state: Boolean?) : Translatable {
        RANDOM(null),
        ALWAYS(true),
        NEVER(false),
        ;

        fun select(state: AvatarRenderState) = this.state ?: (state.hash % 2 == 0)
        override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_animals.boolean_state.${name.lowercase()}"
    }

}
