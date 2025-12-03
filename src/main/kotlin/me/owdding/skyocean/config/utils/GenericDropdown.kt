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
import com.teamresourceful.resourcefulconfigkt.api.builders.StringBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import me.owdding.skyocean.utils.Utils.id
import me.owdding.skyocean.utils.Utils.unsafeCast
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Block
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
            types.sortedBy { toString(it) },
            { types.find { toString.invoke(it) == entry?.string } },
            { selected -> entry?.string = toString.invoke(selected!!) },
        ),
    )

    companion object {
        private data class TranslatableWrapper<T>(val value: T, val converter: (T) -> String) : Translatable {
            override fun getTranslationKey(): String = converter(value)

            companion object {

                fun <Type : EntityType<out E>, E : Entity> entityWrapper(type: Type) = TranslatableWrapper(type) {
                    EntityType.getKey(it).toLanguageKey("entity")
                }
                fun blockWrapper(block: Block) = TranslatableWrapper(block) { it.descriptionId }
            }
        }

        fun EntriesBuilder.entityTypeDropdown(
            default: EntityType<*>,
            options: List<EntityType<*>> = BuiltInRegistries.ENTITY_TYPE.toList(),
            id: String? = null,
            init: TypeBuilder.() -> Unit = {},
        ): ConfigDelegateProvider<RConfigKtEntry<EntityType<*>>> = this.cachedTransform(
            entry = this.genericDropdown<TranslatableWrapper<EntityType<*>>>(
                default = TranslatableWrapper.entityWrapper(default),
                options = options.map { TranslatableWrapper.entityWrapper(it) },
                toString = { EntityType.getKey(it.value).toString() },
                fromString = { TranslatableWrapper.entityWrapper(EntityType.byString(it).getOrNull()!!.unsafeCast()) },
                id = id,
                init = init,
            ),
            from = { TranslatableWrapper.entityWrapper(it) },
            to = { it.value },
        )

        fun EntriesBuilder.blockDropdown(
            default: Block,
            options: List<Block> = BuiltInRegistries.BLOCK.toList(),
            id: String? = null,
            init: TypeBuilder.() -> Unit = {},
        ): ConfigDelegateProvider<RConfigKtEntry<Block>> = this.cachedTransform(
            entry = this.genericDropdown(
                default = TranslatableWrapper.blockWrapper(default),
                options = options.map { TranslatableWrapper.blockWrapper(it) },
                toString = { it.value.id.toString() },
                fromString = { TranslatableWrapper.blockWrapper(BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(it))) },
                id = id,
                init = init,
            ),
            from = { TranslatableWrapper.blockWrapper(it) },
            to = { it.value },
        )

        fun <T : Any> EntriesBuilder.genericDropdown(
            default: T,
            options: List<T>,
            toString: (T) -> String,
            fromString: (String) -> T?,
            id: String? = null,
            init: TypeBuilder.() -> Unit = {},
        ): CachedTransformedEntry<String, T> {
            val renderer = ResourceLocation.fromNamespaceAndPath("skyocean_dropdown", UUID.randomUUID().toString())
            ResourcefulConfigUI.registerElementRenderer(renderer) { element -> GenericDropdown(element, options, toString) }

            val init: StringBuilder.() -> Unit = {
                this.renderer = renderer
                this.init()
            }

            return this.cachedTransform(
                if (id == null) this.string(toString.invoke(default), init)
                else this.string(id, toString.invoke(default), init),
                toString,
            ) { fromString.invoke(it) ?: default }
        }
    }
}

