package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.hash
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.TropicalFish

@RegisterAnimalModifier
object TropicalFishModifier : AnimalModifier<TropicalFish, TropicalFishRenderState> {
    override val type: EntityType<TropicalFish> = EntityType.TROPICAL_FISH
    val patterns = TropicalFish.Pattern.entries

    var tropicalPattern = PlayerAnimalConfig.createEntry("tropical_pattern") { id, type ->
        enum(id, Pattern.RANDOM) {
            this.translation = createTranslationKey("tropical_fish", "${type}_pattern")
            condition = isSelected(EntityType.TROPICAL_FISH)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: TropicalFishRenderState,
        partialTicks: Float,
    ) {
        val randomSource = RandomSource.create(avatarState.hash.toLong())
        state.baseColor = randomSource.nextInt()
        state.patternColor = randomSource.nextInt()
        state.pattern = tropicalPattern.select(avatarState).pattern ?: getRandom(avatarState, patterns)
    }

    enum class Pattern(val pattern: TropicalFish.Pattern?) : Translatable {
        RANDOM(null),

        KOB(TropicalFish.Pattern.KOB),
        SUNSTREAK(TropicalFish.Pattern.SUNSTREAK),
        SNOOPER(TropicalFish.Pattern.SNOOPER),
        DASHER(TropicalFish.Pattern.DASHER),
        BRINELY(TropicalFish.Pattern.BRINELY),
        SPOTTY(TropicalFish.Pattern.SPOTTY),
        FLOPPER(TropicalFish.Pattern.FLOPPER),
        STRIPEY(TropicalFish.Pattern.STRIPEY),
        GLITTER(TropicalFish.Pattern.GLITTER),
        BLOCKFISH(TropicalFish.Pattern.BLOCKFISH),
        BETTY(TropicalFish.Pattern.BETTY),
        CLAYFISH(TropicalFish.Pattern.CLAYFISH),
        ;

        override fun getTranslationKey(): String = createTranslationKey("tropical_fish", "pattern", name)
    }

}
