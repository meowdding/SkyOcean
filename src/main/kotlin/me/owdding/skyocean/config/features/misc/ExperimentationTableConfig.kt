package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object ExperimentationTableConfig : CategoryKt("experimentation") {
    override val name get() = Translated("skyocean.config.misc.experimentation")

    const val SUPERPAIRS_TRANSLATION_KEY = "skyocean.config.misc.experimentation.superpairs"
    val superpairsSolver by boolean(false) {
        this.translation = SUPERPAIRS_TRANSLATION_KEY
    }

    const val CHRONOMATRON_TRANSLATION_KEY = "skyocean.config.misc.experimentation.chronomatron"
    val chronomatronSolver by boolean(false) {
        this.translation = CHRONOMATRON_TRANSLATION_KEY
    }

    const val ULTRASEQUENCER_TRANSLATION_KEY = "skyocean.config.misc.experimentation.ultrasequencer"
    val ultrasequencerSolver by boolean(false) {
        this.translation = ULTRASEQUENCER_TRANSLATION_KEY
    }


}
