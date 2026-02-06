package me.owdding.skyocean.features.hotkeys.system

import me.owdding.ktcodecs.FieldNames
import me.owdding.ktcodecs.GenerateCodec
import net.minecraft.client.Minecraft

@GenerateCodec
data class KeybindSettings(
    @FieldNames("order_sensitive") var orderSensitive: Boolean = false,
    @FieldNames("allow_extra_keys") var allowExtraKeys: Boolean = false,
    var priority: Int = 0,
    var context: ConflictContext = ConflictContext.GLOBAL,
)

enum class ConflictContext {
    GLOBAL {
        override val isActive: Boolean = true
        override fun conflictsWith(other: ConflictContext) = true
    },
    GUI {
        override val isActive: Boolean get() = Minecraft.getInstance().screen != null
        override fun conflictsWith(other: ConflictContext): Boolean = other == GUI
    },
    IN_GAME {
        override val isActive: Boolean get() = Minecraft.getInstance().screen == null
        override fun conflictsWith(other: ConflictContext): Boolean = other == IN_GAME
    },
    ;

    abstract val isActive: Boolean
    abstract fun conflictsWith(other: ConflictContext): Boolean

    fun next() = VALUES[(ordinal + 1) % VALUES.size]

    companion object {
        private val VALUES = entries
    }
}
