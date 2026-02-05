package me.owdding.skyocean.features.hotkeys.system

import com.google.common.collect.EvictingQueue
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.hotkeys.IgnoreHotkeyInputs
import me.owdding.skyocean.features.hotkeys.IslandSpecificHotkeyScreen
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.debugToggle
import me.owdding.skyocean.utils.storage.DataStorage
import net.minecraft.client.input.KeyEvent
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.platform.drawString
import java.util.UUID
import java.util.function.Function

@Module
object HotkeyManager {

    val defaultCategory = HotkeyCategory(
        Util.NIL_UUID,
        "Uncategorized"
    )
    val categories: MutableSet<HotkeyCategory> get() = storage.get().categories

    @IncludedCodec(named = "hotkey_set")
    val hotkeySet: Codec<HashSet<Hotkey>> = CodecHelpers.mutableSet<Hotkey>().xmap(::HashSet, Function.identity())

    @GenerateCodec
    @NamedCodec("HotkeyData")
    data class StoredData(
        val categories: MutableSet<HotkeyCategory>,
        @NamedCodec("hotkey_set") val hotkeys: HashSet<Hotkey>,
    )

    private val storage: DataStorage<StoredData> = DataStorage(
        { StoredData(mutableSetOf(), HashSet()) },
        "hotkeys",
        Codec.withAlternative(
            SkyOceanCodecs.HotkeyDataCodec.codec(),
            hotkeySet.xmap({ StoredData(mutableSetOf(), it) }, { it.hotkeys }),
        ),
    )

    private val tree = HotkeyTree()

    const val MAX_INPUT_DELAY = 250

    private var unorderedKeybinds = mutableListOf<Hotkey>()
    private var buffer: EvictingQueue<InputConstants.Key> = EvictingQueue.create(5)
    private var lastUpdated: Long = 0
    private var pressedKeys: MutableSet<InputConstants.Key> = HashSet()

    init {
        storage.get().hotkeys.forEach(::registerInternal)
    }

    fun hotkeys() = storage.get().hotkeys

    fun registerInternal(hotkey: Hotkey) {
        if (hotkey.keybind.settings.orderSensitive) {
            tree.put(hotkey)
            buffer = EvictingQueue.create(tree.maxDepth())
        } else {
            unorderedKeybinds.add(hotkey)
            unorderedKeybinds.sortByDescending { (keybind, _) -> keybind.settings.priority }
        }
    }

    fun register(hotkey: Hotkey) {
        registerInternal(hotkey)
        this.storage.edit {
            hotkeys.add(hotkey)
        }
    }

    fun unregister(hotkey: Hotkey) {
        tree.remove(hotkey)
        unorderedKeybinds.remove(hotkey)
        buffer = EvictingQueue.create(tree.maxDepth())

        this.storage.edit {
            hotkeys.remove(hotkey)
        }
    }

    @Subscription(TickEvent::class)
    fun tick() {
        if (buffer.isEmpty()) return
        if (lastUpdated + MAX_INPUT_DELAY > System.currentTimeMillis()) return
        invokeValid()
        this.buffer.clear()
    }

    fun invokeValid() {
        for (keys in getOptions()) {
            val hotkeys = tree.get(keys) ?: continue
            val hotkey = hotkeys.find { it.keybind.settings.context.isActive && it.isActive() } ?: continue
            hotkey.invoke()
            break
        }
    }

    fun getOptions(): List<List<InputConstants.Key>> {
        val buffer = buffer.toList().filterNotNull()
        return (0 until buffer.size - 1).map { start ->
            buildList {
                for (i in start until buffer.size) add(buffer[i])
            }
        }
    }

    @JvmStatic
    fun handle(action: Int, event: KeyEvent): Boolean {
        if (McScreen.self is IgnoreHotkeyInputs) return false
        val key by lazy { InputConstants.getKey(event) }
        if (action == GLFW.GLFW_RELEASE) {
            this.pressedKeys.remove(key)
        }
        if (action != GLFW.GLFW_PRESS) return false
        lastUpdated = System.currentTimeMillis()
        buffer.add(key)
        pressedKeys.add(key)

        val hotkey = this.unorderedKeybinds.find {
            it.isActive() && it.keybind.keys.all { key ->
                key in pressedKeys
            } && (
                it.keybind.settings.allowExtraKeys || pressedKeys.all { key ->
                    key in it.keybind.keys
                }
                )
        } ?: return false

        hotkey.invoke()
        clearBuffers()

        return true
    }

    fun clearBuffers() {
        this.buffer.clear()
        this.pressedKeys.clear()
    }

    @JvmStatic
    fun releaseAll() = clearBuffers()

    @Subscription
    fun registerCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("keybinds") {
            McClient.setScreenAsync { IslandSpecificHotkeyScreen }
        }
    }

    val debug by debugToggle("hotkey/buffers", "Shows the values of the current hotkey buffers")

    @Subscription
    fun renderDebug(event: RenderHudEvent) {
        if (!debug) return
        val graphics = event.graphics
        var width = 10
        val plusWidth = McFont.width(" + ")
        val pressedKeys = pressedKeys
        for ((index, key) in pressedKeys.withIndex()) {
            graphics.drawString(key.name, width, 10)
            width += McFont.width(key.name)
            if (index + 1 < pressedKeys.size) {
                graphics.drawString(" + ", width, 10)
                width += plusWidth
            }
        }

        width = 10
        val buffer = buffer.toList()
        for ((index, key) in buffer.withIndex()) {
            graphics.drawString(key.name, width, 20)
            width += McFont.width(key.name)
            if (index + 1 < buffer.size) {
                graphics.drawString(" + ", width, 20)
                width += plusWidth
            }
        }
    }

    fun createCategory(name: String, madeBy: String): HotkeyCategory {
        val category = HotkeyCategory(UUID.randomUUID(), name, madeBy)
        this.storage.edit {
            categories.add(category)
        }
        return category
    }

    fun deleteCategory(category: HotkeyCategory) {
        this.storage.edit {
            hotkeys.removeAll { it.group == category.identifier }
            categories.removeIf {  it.identifier == category.identifier }
        }
    }
}
