package me.owdding.skyocean.helpers

import net.minecraft.world.entity.Entity
import kotlin.time.Duration

internal interface EntityRenderStateAccessor {
    fun `ocean$getNameTagScale`(): Float
    fun `ocean$setNameTagScale`(scale: Float)
}

internal interface EntityAccessor {
    fun `ocean$setGlowing`(glowing: Boolean)
    fun `ocean$setGlowingColor`(color: Int)
    fun `ocean$glowTime`(time: Long)

    fun `ocean$getNameTagScale`(): Float
    fun `ocean$setNameTagScale`(scale: Float)
}

private fun Entity.asAccessor(): EntityAccessor = (this as EntityAccessor)

var Entity.isGlowing: Boolean
    get() = this.isCurrentlyGlowing
    set(value) {
        this.asAccessor().`ocean$setGlowing`(value)
    }

var Entity.glowTime: Duration
    get() = Duration.INFINITE
    set(value) {
        this.asAccessor().`ocean$glowTime`(value.inWholeMilliseconds)
    }

var Entity.glowingColor: Int
    get() = this.teamColor
    set(value) {
        this.asAccessor().`ocean$setGlowingColor`(value)
    }

var Entity.nameTagScale: Float
    get() = this.asAccessor().`ocean$getNameTagScale`()
    set(value) {
        this.asAccessor().`ocean$setNameTagScale`(value)
    }
