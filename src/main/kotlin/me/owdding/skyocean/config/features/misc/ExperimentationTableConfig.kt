package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object ExperimentationTableConfig : CategoryKt("experimentation") {
    override val name get() = Translated("skyocean.config.misc.experimentation")

    val superpairsTranslationKey = "skyocean.config.misc.experimentation.superpairs"
    val superpairsSolver by boolean(false) {
        this.translation = superpairsTranslationKey
    }

    val chronomatronTranslationKey = "skyocean.config.misc.experimentation.chronomatron"
    val chronomatronSolver by boolean(false) {
        this.translation = chronomatronTranslationKey
    }

    val ultrasequencerTranslationKey = "skyocean.config.misc.experimentation.ultrasequencer"
    val ultrasequencerSolver by boolean(false) {
        this.translation = ultrasequencerTranslationKey
    }


}
