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
import net.minecraft.client.renderer.entity.state.RabbitRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.rabbit.Rabbit

@RegisterAnimalModifier
object RabbitModifier : AnimalModifier<Rabbit, RabbitRenderState> {
    override val type: EntityType<Rabbit> = EntityTypes.RABBIT
    private val rabbitVariants = Rabbit.Variant.entries

    var rabbitVariant = PlayerAnimalConfig.createEntry("rabbit_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("rabbit", "${type}_variant")
            condition = isSelected(EntityTypes.RABBIT)
        }
    }

    override fun apply(
        resolver: BlockModelResolver,
        avatarState: AvatarRenderState,
        state: RabbitRenderState,
        partialTicks: Float,
    ) {
        state.variant = rabbitVariant.select(avatarState).variant ?: getRandom(avatarState, rabbitVariants)
    }

    enum class Variant(val variant: Rabbit.Variant?) : Translatable {
        RANDOM(null),

        BROWN(Rabbit.Variant.BROWN),
        WHITE(Rabbit.Variant.WHITE),
        BLACK(Rabbit.Variant.BLACK),
        WHITE_SPLOTCHED(Rabbit.Variant.WHITE_SPLOTCHED),
        GOLD(Rabbit.Variant.GOLD),
        SALT(Rabbit.Variant.SALT),
        EVIL(Rabbit.Variant.EVIL),
        ;

        override fun getTranslationKey(): String = createTranslationKey("rabbit", "variant", name)
    }
}
