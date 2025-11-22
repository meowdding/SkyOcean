package me.owdding.skyocean.features.misc.`fun`.animal

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.config.features.misc.`fun`.FunConfig
import me.owdding.skyocean.generated.SkyOceanAnimalModifiers
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor

object PlayerAnimals {

    private val modifiers: MutableMap<EntityType<*>, AnimalModifier<*, *>> = HashMap()

    lateinit var renderer: LivingEntityRenderer<LivingEntity, LivingEntityRenderState, *>

    init {
        SkyOceanAnimalModifiers.collected.forEach { register(it) }
    }

    @JvmStatic
    fun shouldPlayerBeAnimal(renderState: AvatarRenderState): Boolean {
        val accessor = renderState as? AvatarRenderStateAccessor ?: return false
        return when (FunConfig.playerAnimals) {
            PlayerCatState.NONE -> false
            PlayerCatState.EVERYONE -> true
            PlayerCatState.SELF -> accessor.`skyocean$isSelf`()
            PlayerCatState.PLAYERS -> !accessor.`skyocean$isNpc`()
        }
    }

    private fun <Type : LivingEntity, State : LivingEntityRenderState> register(animalModifier: AnimalModifier<Type, State>) {
        modifiers[animalModifier.type] = animalModifier
    }

    private fun <State : LivingEntityRenderState> getModifier(entityType: EntityType<*>): AnimalModifier<*, State>? = modifiers[entityType].unsafeCast()

    @JvmStatic
    fun <State : LivingEntityRenderState> apply(avatarState: AvatarRenderState, state: State, partialTicks: Float) {
        getModifier<State>(state.entityType)?.apply(avatarState, state, partialTicks)
    }
    @JvmStatic
    fun getEntityType(): EntityType<*> = FunConfig.entityType

    fun createRenderer(context: EntityRendererProvider.Context) {
        renderer = object : LivingEntityRenderer<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>>(context, null, 20f) {
            override fun getTextureLocation(renderState: LivingEntityRenderState): ResourceLocation = SkyOcean.id("none")

            override fun createRenderState(): LivingEntityRenderState? = null
        }
    }


    enum class PlayerCatState : Translatable {
        NONE,
        SELF,
        PLAYERS,

        EVERYONE,
        ;

        override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_animals.state.${name.lowercase()}"
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

    override fun getTranslationKey(): String = "skyocean.config.misc.fun.player_animals.color.${name.lowercase()}"
}
