package me.owdding.skyocean.helpers

import net.minecraft.world.entity.Entity

internal interface EntityAccessor {
    fun `ocean$setGlowing`(glowing: Boolean)
    fun `ocean$setGlowingColor`(color: Int)
}

private fun Entity.asAccessor(): EntityAccessor = (this as EntityAccessor)

var Entity.isGlowing: Boolean
    get() = this.isCurrentlyGlowing
    set(value) {
        this.asAccessor().`ocean$setGlowing`(value)
    }

var Entity.glowingColor: Int
    get() = this.teamColor
    set(value) {
        this.asAccessor().`ocean$setGlowingColor`(value)
    }
