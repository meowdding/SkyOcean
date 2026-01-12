package me.owdding.skyocean.utils

import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils.loadRemoteRepoData
import me.owdding.skyocean.utils.extensions.runCatching
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RemoteRepoDelegate<T : Any>(private val path: String, private val loader: (String) -> T?) : ReadOnlyProperty<Any?, T?> {
    private var cachedValue: AtomicReference<T> = AtomicReference()

    init {
        instances.add(this)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = cachedValue.get()
    private fun update() = runCatching("Loading $path from remote repo!") {
        cachedValue.setRelease(loader(path))
        debug("Loaded $path from remote repo!")
    }

    @Module
    companion object : MeowddingLogger by SkyOcean.featureLogger("RemoteRepo") {
        private val instances: MutableSet<RemoteRepoDelegate<*>> = CopyOnWriteArraySet()

        @Subscription(FinishRepoLoadingEvent::class)
        fun onRepoLoad() {
            instances.forEach { it.update() }
        }

        internal inline fun <reified T : Any> load(path: String) = RemoteRepoDelegate(path) { loadRemoteRepoData<T>(it) }
        internal inline fun <reified T : Any> load(path: String, codec: Codec<T>) = RemoteRepoDelegate(path) { loadRemoteRepoData(it, codec) }
    }
}

