package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.utils.Utils.loadFromRemoteRepo
import me.owdding.skyocean.utils.Utils.loadRemoteRepoData
import me.owdding.skyocean.utils.Utils.loadRepoData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RemoteRepoDelegate<T : Any>(private val path: String, private val loader: (String) -> T?) : ReadOnlyProperty<Any?, T?> {
    private var cachedValue: T? = null
    private var isLoaded = false
    private var lastKnownVersion: Int = 0

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (!isLoaded || lastKnownVersion != version) {
            cachedValue = loader(path)
            lastKnownVersion = version
            isLoaded = true
            println("Loaded remote repo data from $path (version $lastKnownVersion)")
        }
        println(cachedValue)
        return cachedValue
    }

    @Module
    companion object {
        var version: Int = 0
            private set

        @Subscription(FinishRepoLoadingEvent::class)
        fun onRepoLoad() { version++ }

        internal inline fun <reified T : Any> load(path: String) = RemoteRepoDelegate(path) { loadRemoteRepoData<T>(it) }
    }
}

