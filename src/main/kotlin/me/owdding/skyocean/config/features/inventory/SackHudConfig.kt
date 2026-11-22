package me.owdding.skyocean.config.features.inventory

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.features.inventory.sackhud.SackHudEditScreen
import tech.thatgravyboat.skyblockapi.helpers.McClient

object SackHudConfig : ObjectKt(), Translatable {
    private const val PATH = "skyocean.config.inventory.sack_hud"
    override fun getTranslationKey(): String = "$PATH.edit"

    var enabled by boolean(true) {
        this.translation = PATH
    }

    var background by boolean(true) {
        this.translation = "$PATH.background"
    }

    var editorTheme by enum(EditorTheme.DARK) {
        this.translation = "$PATH.editor_theme"
    }

    init {
        button {
            this.title = "$PATH.open_editor"
            this.text = "$PATH.open_editor.text"
            this.description = "$PATH.open_editor.desc"
            onClick {
                McClient.setScreenAsync { SackHudEditScreen() }
            }
        }
    }

    enum class EditorTheme : Translatable {
        LIGHT,
        DARK,
        ;

        override fun getTranslationKey() = "$PATH.editor_theme.${this.name.lowercase()}"
    }
}
