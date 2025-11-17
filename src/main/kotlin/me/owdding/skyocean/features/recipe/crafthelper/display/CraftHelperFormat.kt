package me.owdding.skyocean.features.recipe.crafthelper.display

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable

enum class CraftHelperFormat : Translatable {
    TREE,
    RAW,
    ;

    override fun getTranslationKey(): String = "skyocean.config.misc.crafthelper.tree_formatter.${name.lowercase()}"
}
