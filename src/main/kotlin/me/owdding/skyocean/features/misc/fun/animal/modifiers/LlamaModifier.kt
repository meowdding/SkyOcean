package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.LlamaRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.horse.Llama

@RegisterAnimalModifier
object LlamaModifier : AnimalModifier<Llama, LlamaRenderState> {
    override val type: EntityType<Llama> = EntityType.LLAMA
    val variants = Llama.Variant.entries

    var llamaVariant = PlayerAnimalConfig.createEntry("llama_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("llama", "${type}_variant")
            condition = isSelected(EntityType.LLAMA)
        }
    }

    var llamaTrader = PlayerAnimalConfig.createEntry("llama_trader") { id, type ->
        enum(id, AnimalModifier.BooleanState.RANDOM) {
            this.translation = createTranslationKey("llama", "${type}_trader")
            condition = isSelected(EntityType.LLAMA)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: LlamaRenderState,
        partialTicks: Float,
    ) {
        state.isTraderLlama = llamaTrader.select(avatarState).select(avatarState)
        state.variant = llamaVariant.select(avatarState).variant ?: getRandom(avatarState, variants)
    }


    enum class Variant(val variant: Llama.Variant?) : Translatable {
        RANDOM(null),
        CREAMY(Llama.Variant.CREAMY),
        WHITE(Llama.Variant.WHITE),
        BROWN(Llama.Variant.BROWN),
        GRAY(Llama.Variant.GRAY),
        ;

        override fun getTranslationKey(): String = createTranslationKey("llama", "variant", name)
    }
}
