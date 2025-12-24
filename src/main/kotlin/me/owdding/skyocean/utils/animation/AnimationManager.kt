package me.owdding.skyocean.utils.animation

import me.owdding.lib.extensions.removeIf
import me.owdding.skyocean.mixins.ScreenAccessor
import me.owdding.skyocean.utils.MathUtils
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.isDistantPast

data class AnimationManager(val screen: Screen, val time: Duration, var current: DeferredLayout<*>, val easingFunction: EasingFunction) {
    init {
        current.apply(true)
        current.getLayout().visitWidgets {
            (screen as ScreenAccessor).`skyocean$addRenderableWidget`(it)
        }
    }

    data class State(
        val x: Int, val y: Int, val width: Int, val height: Int,
    )

    var timeStarted = currentInstant()
    var toAdd: MutableList<AbstractWidget> = mutableListOf()
    val interpolated: MutableMap<AbstractWidget, Pair<State, State>> = mutableMapOf()
    var next: DeferredLayout<*>? = null
        set(value) {
            if (field != null && value != null) return
            field = value
            value ?: run {
                toAdd.clear()
                timeStarted = Instant.DISTANT_PAST
                return
            }
            timeStarted = currentInstant()
            val currentElements: MutableList<AbstractWidget> = mutableListOf()
            val nextElements: MutableList<AbstractWidget> = mutableListOf()
            current.getLayout().visitWidgets { currentElements.add(it) }
            value.getLayout().visitWidgets { nextElements.add(it) }

            val accessor = screen as ScreenAccessor
            currentElements.filterNot { nextElements.contains(it) }.forEach { accessor.`skyocean$removeWidget`(it) }
            toAdd.addAll(nextElements.filterNot { currentElements.contains(it) })

            val commonElements = nextElements.filter { currentElements.contains(it) }
            val currentStates = commonElements.withStates()
            val currentLayoutElement = current.getLayout()
            value.getLayout().setPosition(currentLayoutElement.x, currentLayoutElement.y)

            applyImmediately.clear()
            value.apply(true)
            val onPercentage = LinkedHashMap(onPercentage)
            val onFinish = ArrayList(onFinish)
            val nextStates = commonElements.withStates()
            applyImmediately.forEach { accessor.`skyocean$addRenderableWidget`(it) }
            toAdd.removeAll(applyImmediately)

            current.apply()
            applyImmediately.clear()
            this.onPercentage = onPercentage
            this.onFinish = onFinish


            interpolated.putAll(currentStates.mapValues { (key, value) -> value to nextStates[key]!! })
        }
    private val applyImmediately: MutableList<AbstractWidget> = mutableListOf()
    private var onFinish: MutableList<() -> Unit> = mutableListOf()
    private var onPercentage: MutableMap<Double, MutableList<() -> Unit>> = mutableMapOf()

    companion object {
        context(manager: AnimationManager) fun <T : AbstractWidget> T.addImmediately() {
            manager.applyImmediately.add(this)
        }

        context(manager: AnimationManager) fun <T : AbstractWidget> T.onFinish(runnable: T.() -> Unit) {
            manager.onFinish.add { this.runnable() }
        }

        context(manager: AnimationManager) fun <T : AbstractWidget> T.onPercentage(percentage: Double, runnable: T.() -> Unit) {
            manager.onPercentage.getOrPut(percentage) { mutableListOf() }.add { runnable() }
        }
    }

    fun List<AbstractWidget>.withStates() = this.associateWith { State(it.x, it.y, it.width, it.height) }

    fun update() {
        if (timeStarted.isDistantPast) return
        val delta = timeStarted.since() / time
        if (delta >= 1) {
            finish()
            return
        }
        onPercentage.removeIf { (percentage, entries) ->
            if (percentage <= delta) {
                entries.forEach { it() }
                true
            } else false
        }
        val easedDelta = easingFunction(delta)
        interpolated.forEach { widget, (current, next) ->
            widget.x = MathUtils.lerp(easedDelta, current.x, next.x)
            widget.y = MathUtils.lerp(easedDelta, current.y, next.y)
            widget.width = MathUtils.lerp(easedDelta, current.width, next.width)
            widget.height = MathUtils.lerp(easedDelta, current.height, next.height)
        }
    }

    fun finish() {
        val next = next ?: return
        val accessor = screen as ScreenAccessor
        toAdd.forEach { accessor.`skyocean$addRenderableWidget`(it) }
        toAdd.clear()
        interpolated.clear()
        current = next
        this.next = null
        current.apply(true)
        onPercentage.forEach { (_, entries) -> entries.forEach { it() } }
        onFinish.forEach { it() }
        timeStarted = Instant.DISTANT_PAST
    }

}
