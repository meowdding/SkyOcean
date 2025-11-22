package me.owdding.skyocean.config.features.misc.`fun`

import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.RConfigKtEntry
import kotlin.reflect.KProperty
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.features.misc.`fun`.animal.CatModifier
import me.owdding.skyocean.features.misc.`fun`.animal.FoxModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.EntityType

object PlayerAnimalConfig : ObjectKt() {
    private fun isSelected(type: EntityType<*>) = { FunConfig.entityType == type }
    private fun isAnySelected(vararg types: EntityType<*>) = { FunConfig.entityType in types }

    class TargetedValue<T>(val id: String, val constructor: PlayerAnimalConfig.(id: String, type: String) -> ConfigDelegateProvider<RConfigKtEntry<T>>) {
        private operator fun ConfigDelegateProvider<RConfigKtEntry<T>>.provideDelegate(value: TargetedValue<T>, prop: KProperty<*>): RConfigKtEntry<T> {
            return this.provideDelegate(PlayerAnimalConfig, prop)
        }

        var self by PlayerAnimalConfig.constructor("${id}_own", "own")
        var other by PlayerAnimalConfig.constructor("${id}_other", "other")

        fun select(avatarRenderState: AvatarRenderState) = if (AvatarRenderStateAccessor.isSelf(avatarRenderState)) self else other
    }

    var catVariant = TargetedValue("cat_variant") { id, type ->
        enum(id, CatModifier.Variant.DEFAULT) {
            this.translation = "skyocean.config.misc.fun.player_animals.cat.${type}_variant"
            condition = isSelected(EntityType.CAT)
        }
    }

    var foxVariant = TargetedValue("fox_variant") { id, type ->
        enum(id, FoxModifier.Variant.RANDOM) {
            this.translation = "skyocean.config.misc.fun.player_animals.cat.${type}_variant"
            condition = isSelected(EntityType.FOX)
        }
    }

    var collarColor = TargetedValue("color_color") { id, type ->
        enum(id, CatModifier.CollarColor.DEFAULT) {
            this.translation = "skyocean.config.misc.fun.player_animals.cat.${type}_collar"
            condition = isAnySelected(EntityType.CAT, EntityType.WOLF)
        }
    }
}
