package me.owdding.skyocean.config.utils

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.client.options.ResourcefulConfigOptionUI
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfigkt.api.CachedTransformedEntry
import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.RConfigKtEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class GenericDropdown<T>(
    val element: ResourcefulConfigElement,
    val types: List<T>,
    val toString: (T) -> String,
) : ResourcefulConfigElementRenderer {
    private val entry: ResourcefulConfigValueEntry? get() = (element as? ResourcefulConfigEntryElement)?.entry() as? ResourcefulConfigValueEntry

    override fun title(): Component = entry?.options()?.title?.toComponent() ?: Component.empty()
    override fun description(): Component = entry?.options()?.comment?.toComponent() ?: Component.empty()
    override fun widgets(): List<AbstractWidget> = listOf(
        ResourcefulConfigOptionUI.dropdown(
            Component.empty(),
            types,
            { types.find { toString.invoke(it) == entry?.string } },
            { selected -> entry?.string = toString.invoke(selected!!) },
        ),
    )

    companion object {
        private data class EntityTypeWrapper(val entityType: EntityType<out Entity>) : Translatable {
            override fun getTranslationKey(): String = EntityType.getKey(entityType).toLanguageKey("entity")
        }

        fun EntriesBuilder.entityTypeDropdown(
            default: EntityType<*>,
            options: List<EntityType<*>> = BuiltInRegistries.ENTITY_TYPE.toList(),
            init: TypeBuilder.() -> Unit = {},
        ): ConfigDelegateProvider<RConfigKtEntry<EntityType<out Entity>>> = this.cachedTransform(
            this.genericDropdown(
                EntityTypeWrapper(default),
                options.map { EntityTypeWrapper(it) },
                { EntityType.getKey(it.entityType).toString() },
                { EntityTypeWrapper(EntityType.byString(it).getOrNull().unsafeCast()) },
                init,
            ),
            from = { EntityTypeWrapper(it) },
            to = { it.entityType },
        )

        fun <T : Any> EntriesBuilder.genericDropdown(
            default: T,
            options: List<T>,
            toString: (T) -> String,
            fromString: (String) -> T?,
            init: TypeBuilder.() -> Unit = {},
        ): CachedTransformedEntry<String, T> {
            val renderer = ResourceLocation.fromNamespaceAndPath("skyocean_dropdown", UUID.randomUUID().toString())
            ResourcefulConfigUI.registerElementRenderer(renderer) { element -> GenericDropdown(element, options, toString) }
            return this.cachedTransform(
                this.string(toString.invoke(default)) {
                    this.renderer = renderer
                    this.init()
                },
                toString,
            ) { fromString.invoke(it) ?: default }
        }
    }
}

