package me.owdding.skyocean.features.misc.`fun`.animal

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.config.features.misc.`fun`.FunConfig
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.generated.SkyOceanAnimalModifiers
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.client.Minecraft
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemDisplayContext
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

object PlayerAnimals {

    private val modifiers: MutableMap<EntityType<*>, AnimalModifier<*, *>> = HashMap()

    lateinit var renderer: LivingEntityRenderer<LivingEntity, LivingEntityRenderState, *>
    var context: EntityRendererProvider.Context? = null

    fun registerModifiers() {
        SkyOceanAnimalModifiers.collected.forEach { register(it) }
    }

    @JvmStatic
    fun shouldPlayerBeAnimal(renderState: AvatarRenderState): Boolean {
        val accessor = renderState as? AvatarRenderStateAccessor ?: return false
        if (!LocationAPI.isOnSkyBlock && !FunConfig.outsideSkyblock) return false
        return when (FunConfig.playerAnimals) {
            PlayerAnimalState.NONE -> false
            PlayerAnimalState.EVERYONE -> true
            PlayerAnimalState.SELF -> accessor.`skyocean$isSelf`()
            PlayerAnimalState.PLAYERS -> !accessor.`skyocean$isNpc`()
        }
    }

    private fun <Type : LivingEntity, State : LivingEntityRenderState> register(animalModifier: AnimalModifier<Type, State>) {
        modifiers[animalModifier.type] = animalModifier
    }

    private fun <State : LivingEntityRenderState> getModifier(entityType: EntityType<*>): AnimalModifier<*, State>? = modifiers[entityType].unsafeCast()

    @JvmStatic
    fun <State : LivingEntityRenderState> apply(entity: LivingEntity, avatarState: AvatarRenderState, state: State, partialTicks: Float) {
        state.isBaby = PlayerAnimalConfig.isBaby.select(avatarState)
        if (state is ArmedEntityRenderState) {
            ArmedEntityRenderState.extractArmedEntityRenderState(
                entity,
                state,
                Minecraft.getInstance().itemModelResolver,
                /*? > 1.21.10 >>*/partialTicks,
            )
        }
        if (state is HumanoidRenderState) {
            state.swimAmount = avatarState.swimAmount
            state.attackTime = avatarState.attackTime
            state.speedValue = avatarState.speedValue
            state.maxCrossbowChargeDuration = avatarState.maxCrossbowChargeDuration
            state.ticksUsingItem = avatarState.ticksUsingItem
            state.attackArm = avatarState.attackArm
            state.useItemHand = avatarState.useItemHand
            state.isCrouching = avatarState.isCrouching
            state.isFallFlying = avatarState.isFallFlying
            state.isVisuallySwimming = avatarState.isVisuallySwimming
            state.isPassenger = avatarState.isPassenger
            state.isUsingItem = avatarState.isUsingItem
            state.elytraRotX = avatarState.elytraRotX
            state.elytraRotY = avatarState.elytraRotY
            state.elytraRotZ = avatarState.elytraRotZ
            state.headEquipment = avatarState.headEquipment
            state.chestEquipment = avatarState.chestEquipment
            state.legsEquipment = avatarState.legsEquipment
            state.feetEquipment = avatarState.feetEquipment
        }
        if (state is HoldingEntityRenderState) {
            appendItemLayer(state, avatarState)
        }
        getModifier<State>(state.entityType)?.apply(avatarState, state, partialTicks)
    }
    @JvmStatic
    fun getEntityType(): EntityType<*> = FunConfig.entityType

    fun createRenderer(context: EntityRendererProvider.Context) {
        this.context = context
        renderer = object : LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>>(context, null, 20f) {
            override fun getTextureLocation(renderState: LivingEntityRenderState): Identifier = SkyOcean.id("none")
            override fun createRenderState(): LivingEntityRenderState? = null
        }
    }

    fun appendItemLayer(state: HoldingEntityRenderState, avatarState: AvatarRenderState) {
        context?.itemModelResolver?.appendItemLayers(
            state.heldItem,
            AvatarRenderStateAccessor.getHeldItemStack(avatarState),
            ItemDisplayContext.GROUND,
            null,
            null,
            avatarState.id + ItemDisplayContext.GROUND.ordinal,
        )
    }

    enum class PlayerAnimalState : Translatable {
        NONE,
        SELF,
        PLAYERS,

        EVERYONE,
        ;

        override fun getTranslationKey(): String = createTranslationKey("state", name)
    }

}

enum class CollarColor(val dyeColor: DyeColor?) : Translatable {
    DEFAULT(null),
    NONE(null),

    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK),
    ;

    override fun getTranslationKey(): String = createTranslationKey("color", name)
}

enum class AnimalColor(val dyeColor: DyeColor?) : Translatable {
    RANDOM(null),

    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK),
    ;

    override fun getTranslationKey(): String = createTranslationKey("color", name)
}
