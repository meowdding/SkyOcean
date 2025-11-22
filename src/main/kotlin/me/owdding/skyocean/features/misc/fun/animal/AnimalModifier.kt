package me.owdding.skyocean.features.misc.`fun`.animal

import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import tech.thatgravyboat.skyblockapi.platform.texture

interface AnimalModifier<Type : LivingEntity, State : LivingEntityRenderState> {
    fun <T> getRandom(state: AvatarRenderState, list: List<T>) = list[Math.floorMod(state.hash, list.size)]
    val AvatarRenderState.hash get() = AvatarRenderStateAccessor.getUUID(this)?.hashCode() ?: this.skin.texture.hashCode()

    val type: EntityType<Type>

    fun apply(avatarState: AvatarRenderState, state: State, partialTicks: Float)
}
