package me.owdding.skyocean.helpers

import com.google.common.hash.Hashing
import com.google.gson.JsonElement
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.lib.utils.suggestions.MeowddingSuggestionProviders
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Utils.containerItems
import me.owdding.skyocean.utils.Utils.getArgument
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.debugToggle
import me.owdding.skyocean.utils.storage.FolderStorage
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenInitializedEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyPressedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJsonOrThrow
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Suppress("UnstableApiUsage")
@Module
object ChestDump {

    val enabled by debugToggle("chest_dumps", "Allows you to save inventories by pressing 'S'")

    val logger = SkyOcean.featureLogger()

    private val storage = FolderStorage("chest_dumps", SkyOceanCodecs.ChestDumpStorageCodec.codec())

    @Subscription
    fun onKey(event: ScreenKeyPressedEvent.Pre) {
        if (!enabled) return
        if (event.key != InputConstants.KEY_S) return

        val chest = (event.screen as? AbstractContainerScreen<*>)?.menu as? ChestMenu ?: return
        val title = event.screen.title
        val items = chest.slots.containerItems()

        val hash = Hashing.sha256().newHasher()
        hash.putUnencodedChars(title.string)
        items.forEach { hash.putUnencodedChars(it.displayName.string) }

        val type = BuiltInRegistries.MENU.getKey(chest.type) ?: run {
            logger.error("meow")
            return
        }

        storage.set(hash.hash().asBytes().toHexString(), ChestDumpStorage(title, items.toJsonOrThrow(CodecHelpers.ITEM_STACK_CODEC.listOf()), type))
    }


    fun <T : AbstractContainerMenu> createScreen(type: MenuType<T>, dump: ChestDumpStorage): Screen = when (type) {
        MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3, MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6 -> {
            val menu = type.create(-1, McPlayer.self!!.inventory)
            object : ContainerScreen(menu as ChestMenu, McPlayer.self!!.inventory, dump.title) {
                override fun init() {
                    super.init()
                    ScreenInitializedEvent(this).post(SkyBlockAPI.eventBus)
                    dump.items.toDataOrThrow(CodecHelpers.ITEM_STACK_CODEC.listOf()).forEachIndexed { index, item ->
                        menu.slots[index].set(item)
                        InventoryChangeEvent(item, menu.slots[index], dump.title, menu.slots, this).post(SkyBlockAPI.eventBus)
                    }
                }
            }
        }

        else -> {
            throw UnsupportedOperationException("Unsupported menu type: $type")
        }
    }

    fun openDump(dump: ChestDumpStorage) {
        val type = BuiltInRegistries.MENU.getValue(dump.type)!!
        val screen = createScreen(type, dump)
        McClient.setScreenAsync { screen }
    }

    @Subscription
    fun command(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("chestdump") {
            val suggestions = MeowddingSuggestionProviders.iterable(storage.getStorages().entries) {
                val dump = it.value.get()
                "${dump.title.stripped}-${it.key.take(3)}"
            }

            fun getDump(id: String) = storage.getAll().entries.find { "${it.value.title.stripped}-${it.key.take(3)}" == id }
            then("open id", StringArgumentType.greedyString(), suggestions) {
                callback {
                    val id = getArgument<String>("id")

                    val dump = getDump(id!!)?.value ?: run {
                        Text.of("No dump with id $id found.").sendWithPrefix()
                        return@callback
                    }

                    openDump(dump)
                }
            }
            then("delete id", StringArgumentType.greedyString(), suggestions) {
                callback {
                    val id = getArgument<String>("id")

                    val dump = getDump(id!!)?.key ?: run {
                        Text.of("No dump with id $id found.").sendWithPrefix()
                        return@callback
                    }

                    Text.of("Deleted dump $id").sendWithPrefix()
                    storage.remove(dump)
                }
            }
            thenCallback("refresh") {
                storage.refresh()
                Text.of("Refreshed chest dumps.").sendWithPrefix()
            }
        }
    }

    @GenerateCodec
    data class ChestDumpStorage(
        val title: Component,
        val items: JsonElement,
        val type: ResourceLocation,
    )

}
