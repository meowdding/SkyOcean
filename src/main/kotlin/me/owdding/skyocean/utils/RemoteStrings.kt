package me.owdding.skyocean.utils

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.lib.events.StartRepoLoadingEvent
import me.owdding.lib.utils.FeatureName
import me.owdding.repo.RemoteRepo
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import kotlin.reflect.KProperty

data class CompletedElementDelegate<Type>(
    val key: String,
    val transform: (String) -> Type,
    val default: Type,
) {
    init {
        RemoteStrings.register(this)
    }

    var data = get()

    fun get() = RemoteStrings.stringOverwrites[key]?.let { transform(it) } ?: default

    fun update() {
        data = get()
    }

    operator fun <This> getValue(ref: This, property: KProperty<*>): Type = data
}

data class ElementDelegate<Type>(
    val path: String?,
    val prefix: String?,
    val transformer: (String) -> Type,
    val default: Type,
) {
    operator fun <This> provideDelegate(ref: This, property: KProperty<*>) = CompletedElementDelegate(
        join(prefix, path ?: property.name) ?: property.name,
        transformer,
        default,
    )
}

private fun join(vararg parts: String?) = listOfNotNull(*parts).takeUnless { it.isEmpty() }?.joinToString(".")

interface StringProvider {
    fun string(default: String, path: String? = null, prefix: String? = null): ElementDelegate<String>

    fun regex(regex: Regex, path: String? = null, prefix: String? = null): ElementDelegate<Regex>
    fun regex(@Language("RegExp") regex: String, path: String? = null, prefix: String? = null): ElementDelegate<Regex> = regex(regex.toRegex(), path, prefix)
    fun resolve(path: String): StringProvider

    companion object {
        internal val STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        fun StringProvider.resolve(): StringProvider {
            val name = STACK_WALKER.callerClass.let { it.annotations.filterIsInstance<FeatureName>().firstOrNull()?.name ?: it.simpleName }

            return resolve(name)
        }
    }
}

data class StringProviderImpl(val prefix: String, val parent: StringProvider) : StringProvider {
    override fun string(default: String, path: String?, prefix: String?): ElementDelegate<String> = parent.string(default, path, join(this.prefix, prefix))
    override fun regex(regex: Regex, path: String?, prefix: String?): ElementDelegate<Regex> = parent.regex(regex, path, join(this.prefix, prefix))
    override fun resolve(path: String): StringProvider = StringProviderImpl(join(prefix, path) ?: path, this)
}

@Module
object RemoteStrings : StringProvider {
    private val list: MutableList<CompletedElementDelegate<*>> = mutableListOf()
    val stringOverwrites: MutableMap<String, String> = mutableMapOf()

    @Subscription(StartRepoLoadingEvent::class)
    fun repoStartLoading() {
        stringOverwrites.clear()
        list.forEach(CompletedElementDelegate<*>::update)
    }

    @Subscription(FinishRepoLoadingEvent::class)
    fun repoLoaded() {
        val data = RemoteRepo.getFileContentAsJson("skyocean/strings.json") as? JsonObject ?: return

        val map = HashMap<String, String>()
        data.discover("", map::put)
        stringOverwrites.putAll(map)
        list.forEach(CompletedElementDelegate<*>::update)
    }

    fun JsonObject.discover(path: String, consumer: (String, String) -> Unit) {
        this.entrySet().forEach { (key, value) ->

            val path = listOfNotNull(path.takeUnless { it.isEmpty() }, key).joinToString(".")

            when (value) {
                is JsonObject -> value.discover(path, consumer)
                is JsonPrimitive -> consumer(key, value.asString)
            }
        }
    }

    override fun string(default: String, path: String?, prefix: String?): ElementDelegate<String> = ElementDelegate(path, prefix, transformer = { it }, default)

    override fun regex(regex: Regex, path: String?, prefix: String?): ElementDelegate<Regex> = ElementDelegate(path, prefix, transformer = { Regex(it) }, regex)

    override fun resolve(path: String): StringProvider = StringProviderImpl(path, this)

    fun register(delegate: CompletedElementDelegate<*>) {
        list.add(delegate)
    }
}
