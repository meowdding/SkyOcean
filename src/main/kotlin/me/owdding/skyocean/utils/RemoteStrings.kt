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
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
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

interface StringGroup {
    fun string(default: String, path: String? = null, prefix: String? = null): ElementDelegate<String>

    fun componentRegex(@Language("RegExp") regex: String, path: String? = null, prefix: String? = null): ElementDelegate<ComponentRegex> =
        componentRegex(regex.toRegex(), path, prefix)

    fun componentRegex(regex: Regex, path: String? = null, prefix: String? = null): ElementDelegate<ComponentRegex>
    fun regex(regex: Regex, path: String? = null, prefix: String? = null): ElementDelegate<Regex>
    fun regex(@Language("RegExp") regex: String, path: String? = null, prefix: String? = null): ElementDelegate<Regex> = regex(regex.toRegex(), path, prefix)
    fun resolve(path: String): StringGroup

    companion object {
        internal val STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        fun StringGroup.resolve(): StringGroup {
            val name = STACK_WALKER.callerClass.let { it.annotations.filterIsInstance<FeatureName>().firstOrNull()?.name ?: it.simpleName }

            return resolve(name)
        }
    }
}

data class StringGroupImpl(val prefix: String, val parent: StringGroup) : StringGroup {
    override fun string(default: String, path: String?, prefix: String?): ElementDelegate<String> = parent.string(default, path, join(this.prefix, prefix))
    override fun regex(regex: Regex, path: String?, prefix: String?): ElementDelegate<Regex> = parent.regex(regex, path, join(this.prefix, prefix))
    override fun componentRegex(regex: Regex, path: String?, prefix: String?): ElementDelegate<ComponentRegex> =
        parent.componentRegex(regex, path, join(this.prefix, prefix))

    override fun resolve(path: String): StringGroup = StringGroupImpl(join(prefix, path) ?: path, this)
}

@Module
object RemoteStrings : StringGroup {
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

    override fun componentRegex(
        regex: Regex,
        path: String?,
        prefix: String?,
    ): ElementDelegate<ComponentRegex> = ElementDelegate(path, prefix, transformer = { ComponentRegex(it) }, ComponentRegex(regex))

    override fun regex(regex: Regex, path: String?, prefix: String?): ElementDelegate<Regex> = ElementDelegate(path, prefix, transformer = { Regex(it) }, regex)

    override fun resolve(path: String): StringGroup = StringGroupImpl(path, this)

    fun register(delegate: CompletedElementDelegate<*>) {
        list.add(delegate)
    }
}
