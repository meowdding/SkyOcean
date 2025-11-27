package me.owdding.skyocean.features.item.custom.data

import net.minecraft.util.TriState
import net.minecraft.world.item.equipment.Equippable

/**
 * A state that holds weather a given equippable should fall back to vanilla logic or be replaced.
 * When state is DEFAULT it should use completely vanilla logic*.
 * When state is TRUE it should use the supplied equippable for the vanilla logic.
 * When state is FALSE it should return false for the check and not use vanilla logic.
 */
data class EquippableModelState(
    val state: TriState,
    val equippable: Equippable?,
) {

    companion object {
        val VANILLA = EquippableModelState(TriState.DEFAULT, null)
        val NON_EQUIPPABLE = EquippableModelState(TriState.FALSE, null)
    }
}
