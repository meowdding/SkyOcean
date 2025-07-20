package me.owdding.skyocean.helpers

import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.lib.compat.REIRenderOverlayEvent
import me.owdding.lib.layouts.BackgroundWidget
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.mixins.ScreenAccessor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerInitializedEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.extentions.top
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

abstract class InventorySideGui(@Language("RegExp") titleRegex: String) {

    private val regex = titleRegex.toRegex()
    var oldList: LayoutWidget<*>? = null
    var oldWidget: AbstractWidget? = null

    abstract val enabled: Boolean

    init {
        SkyBlockAPI.eventBus.register<REIRenderOverlayEvent> { reiBeingAStupidMod(it) }
        SkyBlockAPI.eventBus.register<ContainerCloseEvent> { onContainerClose() }
        SkyBlockAPI.eventBus.register<ScreenInitializedEvent> { onScreenInit(it) }
        SkyBlockAPI.eventBus.register<ContainerInitializedEvent>(priority = Subscription.LOW) { onInvChange(it) }
    }

    protected abstract fun ContainerInitializedEvent.getLayout(): Layout?

    private fun onInvChange(event: ContainerInitializedEvent) {
        if (!enabled) return
        if (!regex.matches(event.screen.title.stripped)) return
        val screen = event.screen

        val layout = event.getLayout() ?: return
        val widget = BackgroundWidget(SkyOcean.id("blank"), layout, 5).apply { this.setPosition(screen.right + 5, screen.top) }
        screen.addWidget(widget)
    }

    // Used so that when Hypixel resends the entire screen we can
    // show the list before the items are resent so it doesn't
    // fall in and out.
    private fun onScreenInit(event: ScreenInitializedEvent) {
        if (!enabled) return
        if (!regex.matches(event.screen.title.stripped)) return
        val widget = this.oldWidget ?: return

        event.screen.addWidget(widget)
    }

    private fun Screen.addWidget(widget: AbstractWidget) {
        val accessor = this as ScreenAccessor
        oldWidget?.let { accessor.`skyocean$removeWidget`(it) }
        oldWidget = widget
        this.`skyocean$addRenderableWidget`(widget)
    }

    private fun reiBeingAStupidMod(event: REIRenderOverlayEvent) {
        oldWidget?.let {
            event.register(it.x, it.y, it.width, it.height)
        }
    }

    private fun onContainerClose() {
        oldWidget = null
        oldList = null
    }

}
