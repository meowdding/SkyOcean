package me.owdding.skyocean.config.features.misc.`fun`

import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.RConfigKtEntry
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor
import me.owdding.skyocean.features.misc.`fun`.animal.CollarColor
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.EntityType
import kotlin.reflect.KProperty

object PlayerAnimalConfig : ObjectKt() {
    class TargetedValue<T>(val id: String, val constructor: PlayerAnimalConfig.(id: String, type: String) -> ConfigDelegateProvider<RConfigKtEntry<T>>) {
        var self by PlayerAnimalConfig.constructor("${id}_own", "own")

        var other by PlayerAnimalConfig.constructor("${id}_other", "other")

        fun select(avatarRenderState: AvatarRenderState) = if (AvatarRenderStateAccessor.isSelf(avatarRenderState)) self else other
        private operator fun ConfigDelegateProvider<RConfigKtEntry<T>>.provideDelegate(
            value: TargetedValue<T>,
            prop: KProperty<*>,
        ): RConfigKtEntry<T> = this.provideDelegate(PlayerAnimalConfig, prop)
    }

    var collarColor = TargetedValue("collar_color") { id, type ->
        enum(id, CollarColor.DEFAULT) {
            this.translation = "skyocean.config.misc.fun.player_animals.config.${type}_collar"
            condition = isAnySelected(EntityType.CAT, EntityType.WOLF)
        }
    }

    var isBaby = TargetedValue("baby") { id, type ->
        boolean(id, false) {
            this.translation = "skyocean.config.misc.fun.player_animals.config.${type}_baby"
        }
    }

    fun <T> createEntry(
        id: String,
        constructor: PlayerAnimalConfig.(id: String, type: String) -> ConfigDelegateProvider<RConfigKtEntry<T>>,
    ) = TargetedValue(id, constructor)

    fun isSelected(type: EntityType<*>) = { FunConfig.entityType == type }
    fun isAnySelected(vararg types: EntityType<*>) = { FunConfig.entityType in types }
}
