package me.owdding.skyocean.features.hotkeys.system

import com.mojang.blaze3d.platform.InputConstants
import java.util.*
import kotlin.math.max

private sealed interface HotkeyTreeNode {
    val isEmpty: Boolean
    fun put(depth: Int, hotkey: Hotkey)
    fun get(depth: Int, path: List<InputConstants.Key>): List<Hotkey>?
    fun remove(depth: Int, hotkey: Hotkey)
    fun getMaxDepth(): Int
}

private class HotkeyTreeBranch : HotkeyTreeNode {
    private val map = IdentityHashMap<InputConstants.Key, HotkeyTreeNode>()
    private var hotkeys: List<Hotkey>? = null
    override val isEmpty: Boolean get() = this.map.isEmpty() && this.hotkeys?.isEmpty() != false

    override fun put(depth: Int, hotkey: Hotkey) {
        val path = hotkey.keybind.keys
        if (depth >= hotkey.keybind.keys.size) {
            val hotkeys = hotkeys
            this.hotkeys = if (hotkeys == null) listOf(hotkey) else listOf(hotkey, *hotkeys.toTypedArray())
            return
        }

        val current = path[depth]
        when (val node = map[current]) {
            is HotkeyTreeBranch -> node.put(depth + 1, hotkey)

            null -> map[current] = HotkeyTreeBranch().apply {
                put(depth + 1, hotkey)
            }
        }
    }

    override fun get(depth: Int, path: List<InputConstants.Key>): List<Hotkey>? {
        if (depth >= path.size) {
            return hotkeys
        }
        val current = path[depth]
        return when (val node = map[current]) {
            is HotkeyTreeBranch -> node.get(depth + 1, path)
            else -> null
        }
    }

    override fun remove(depth: Int, hotkey: Hotkey) {
        val path = hotkey.keybind.keys
        if (depth >= path.size) {
            val hotkeys = hotkeys ?: return
            if (hotkey in hotkeys) {
                this.hotkeys = hotkeys.filterNot { it == hotkey }
            }

            return
        }


        val current = path[depth]
        val node = map[current] ?: return
        when (node) {
            is HotkeyTreeBranch -> node.remove(depth + 1, hotkey)
        }

        if (node.isEmpty) {
            this.map.remove(current)
        }
    }

    override fun getMaxDepth(): Int = max(1, 1 + (map.values.maxOfOrNull { it.getMaxDepth() } ?: 0))
}

class HotkeyTree {
    private val root = HotkeyTreeBranch()
    private var maxDepth = 1

    fun put(keybind: Hotkey) {
        root.put(0, keybind)
        maxDepth = root.getMaxDepth()
    }

    fun remove(keybind: Hotkey) {
        root.remove(0, keybind)
        maxDepth = root.getMaxDepth()
    }

    fun get(path: List<InputConstants.Key>) = root.get(0, path)

    fun maxDepth() = maxDepth
}
