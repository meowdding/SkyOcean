package me.owdding.skyocean.utils.animation

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.*

object DeferredLayoutFactory {
    fun vertical(init: DeferredLinearLayout.() -> Unit = {}) = DeferredLinearLayout(LinearLayout.vertical()).apply(init)
    fun horizontal(alignment: Float = 0f, init: DeferredLinearLayout.() -> Unit = {}) = DeferredLinearLayout(
        LinearLayout.horizontal().apply {
            defaultCellSetting().alignVertically(alignment)
        },
    ).apply(init)

    fun frame(init: DeferredFrameLayout.() -> Unit = {}) = DeferredFrameLayout(FrameLayout()).apply(init)
    fun grid(init: DeferredGridLayout.() -> Unit = {}) = DeferredGridLayout(GridLayout()).apply(init)
}

abstract class DeferredLayout<Type : Layout> {
    protected abstract val _backing: Type
    protected val deferred: MutableList<context(AnimationManager) (MutableList<() -> Unit>) -> Unit> = mutableListOf()
    var onAnimationStart: MutableList<() -> Unit> = mutableListOf()
    abstract fun <T : LayoutElement> add(widget: T)

    companion object {
        context(onAnimationStart: MutableList<() -> Unit>) fun <T : AbstractWidget> T.onAnimationStart(runnable: () -> Unit) {
            onAnimationStart.add(runnable)
        }
    }

    open fun <T : AbstractWidget> add(widget: T, init: context(AnimationManager, DeferredLayout<Type>, MutableList<() -> Unit>) T.() -> Unit) {
        add(widget)
        deferred.add {
            context(it) {
                widget.init()
            }
        }
    }

    fun applyDefault(manager: AnimationManager) = context(manager) { apply(true) }

    context(_: AnimationManager) fun apply(animationStart: Boolean = false) {
        deferred.forEach { runnable -> runnable(onAnimationStart) }
        if (animationStart) {
            onAnimationStart.forEach { it() }
        }
        _backing.arrangeElements()
        onAnimationStart.clear()
    }

    fun getLayout() = _backing

    fun addDeferred(layout: DeferredLayout<*>) {
        deferred.addAll(layout.deferred)
        add(layout._backing)
    }

    fun vertical(init: DeferredLinearLayout.() -> Unit) = addDeferred(DeferredLayoutFactory.vertical(init))
    fun horizontal(alignment: Float = 0f, init: DeferredLinearLayout.() -> Unit) = addDeferred(DeferredLayoutFactory.horizontal(alignment, init))
    fun frame(init: DeferredFrameLayout.() -> Unit) = addDeferred(DeferredLayoutFactory.frame(init))
    fun grid(init: DeferredGridLayout.() -> Unit) = addDeferred(DeferredLayoutFactory.grid(init))

    fun spacer(width: Int = 0, height: Int = 0) {
        add(SpacerElement(width, height))
    }
}

data class DeferredLinearLayout(
    override val _backing: LinearLayout,
) : DeferredLayout<LinearLayout>() {
    override fun <T : LayoutElement> add(widget: T) {
        _backing.addChild(widget)
    }
}

data class DeferredFrameLayout(
    override val _backing: FrameLayout,
) : DeferredLayout<FrameLayout>() {
    override fun <T : LayoutElement> add(widget: T) {
        _backing.addChild(widget)
    }
}

data class DeferredGridLayout(
    override val _backing: GridLayout,
) : DeferredLayout<GridLayout>() {
    override fun <T : LayoutElement> add(widget: T) = throw UnsupportedOperationException("Can't be used with grid layout!")
    override fun <T : AbstractWidget> add(widget: T, init: context(AnimationManager, DeferredLayout<GridLayout>, MutableList<() -> Unit>) T.() -> Unit) =
        throw UnsupportedOperationException("Can't be used with grid layout!")

    fun <T : LayoutElement> add(widget: T, row: Int, column: Int) {
        _backing.addChild(widget, row, column)
    }

    fun <T : AbstractWidget> add(widget: T, row: Int, column: Int, init: T.() -> Unit) {
        add(widget, row, column)
        deferred.add { widget.init() }
    }
}


