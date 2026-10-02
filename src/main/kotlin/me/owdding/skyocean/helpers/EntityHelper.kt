package me.owdding.skyocean.helpers

import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import me.owdding.ktmodules.Module
import net.minecraft.util.ARGB
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

//? renderchest {
/*import net.azureaaron.renderchest.api.CustomGlowCallback
import net.azureaaron.renderchest.api.GlowConstants
*///?}

internal interface EntityRenderStateAccessor {
    fun `ocean$getNameTagScale`(): Float
    fun `ocean$setNameTagScale`(scale: Float)
}

internal interface EntityAccessor {
    fun `ocean$getNameTagScale`(): Float
    fun `ocean$setNameTagScale`(scale: Float)
}

private fun Entity.asAccessor(): EntityAccessor = (this as EntityAccessor)

/**
 * Applies a non-Xray glowing outline to the entity using the specified RGB color.
 * Set the color to null to remove the custom outline.
 * The alpha channel of the color is ignored and the outline is always fully opaque.
 *
 * If the entity already has the vanilla glowing effect,
 * its outline remains visible through walls and its color is overridden.
 * Removing the custom outline does not remove the vanilla glowing effect.
 */
var Entity.glowingColor: Int?
    get() = EntityHelper.getEntityColor(this)
    set(value) {
        if (value == null) {
            EntityHelper.removeEntityColor(this)
        } else {
            EntityHelper.setEntityColor(this, value)
        }
    }

var Entity.nameTagScale: Float
    get() = this.asAccessor().`ocean$getNameTagScale`()
    set(value) {
        this.asAccessor().`ocean$setNameTagScale`(value)
    }

@Module
object EntityHelper {
    private val entityGlowMap = Int2IntOpenHashMap()

    //? renderchest {
    /*init {
        CustomGlowCallback.EVENT.register { entity, _ ->
            entityGlowMap.getOrDefault(entity.id, GlowConstants.NO_GLOW)
        }
   }*///?}

    @Subscription(ServerChangeEvent::class)
    private fun onWorldChange() {
        entityGlowMap.clear()
    }

    @Subscription
    private fun onEntityLeaveWorld(event: EntityRemovedEvent) {
        entityGlowMap.remove(event.entity.id)
    }

    fun removeEntityColor(entity: Entity) {
        val entityId = entity.id
        McClient.runOrNextTick {
            entityGlowMap.remove(entityId)
        }
    }

    fun setEntityColor(entity: Entity, argb: Int) {
        val rgb = ARGB.opaque(argb)
        val entityId = entity.id
        McClient.runOrNextTick {
            entityGlowMap[entityId] = rgb
        }
    }

    fun getEntityColor(entity: Entity): Int? {
        RenderSystem.assertOnRenderThread()
        val rgb = entityGlowMap.getOrDefault(entity.id, 0)
        return if (rgb == 0) null else rgb
    }
}
