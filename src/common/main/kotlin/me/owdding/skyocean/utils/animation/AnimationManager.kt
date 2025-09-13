package me.owdding.skyocean.utils.animation

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
        current.apply()
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
            value.apply()
            val nextStates = commonElements.withStates()
            current.apply()

            toAdd.removeAll(applyImmediately)
            applyImmediately.forEach { accessor.`skyocean$addRenderableWidget`(it) }
            applyImmediately.clear()

            interpolated.putAll(currentStates.mapValues { (key, value) -> value to nextStates[key]!! })
        }
    private var applyImmediately: MutableList<AbstractWidget> = mutableListOf()

    companion object {
        context(manager: AnimationManager) fun <T : AbstractWidget> T.addImmediately() {
            manager.applyImmediately.add(this)
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
        current.getLayout().arrangeElements()
        timeStarted = Instant.DISTANT_PAST
    }

}
