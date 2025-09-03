package me.owdding.skyocean.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.data.*
import me.owdding.skyocean.mixins.ModelManagerAccessor
import me.owdding.skyocean.repo.customization.AnimatedSkulls
import me.owdding.skyocean.repo.customization.DyeData
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.OceanColors
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.Utils.getArgument
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.Utils.wrapWithNotItalic
import me.owdding.skyocean.utils.commands.HexColorArgumentType
import me.owdding.skyocean.utils.commands.SkyOceanItemIdArgument
import me.owdding.skyocean.utils.commands.VirtualResourceArgument
import net.minecraft.commands.arguments.ComponentArgument
import net.minecraft.commands.arguments.ResourceKeyArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.equipment.trim.TrimMaterial
import net.minecraft.world.item.equipment.trim.TrimPattern
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CustomizeCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("customize") {
            then("reset") {
                thenCallback("name") {
                    remove(CustomItemDataComponents.NAME) { item ->
                        text("Removed custom name from ") {
                            append(item.hoverName)
                            append("!")
                        }
                    }
                }
                thenCallback("model") {
                    remove(CustomItemDataComponents.MODEL) { _, model ->
                        text("Removed custom model $model from item!")
                    }
                }
                thenCallback("armor_trim") {
                    remove(CustomItemDataComponents.ARMOR_TRIM) { _ ->
                        text("Removed armor trim from item!")
                    }
                }
                thenCallback("color") {
                    remove(CustomItemDataComponents.COLOR) {
                        text("Removed color from item!")
                    }
                }
                thenCallback("enchantment_glint") {
                    remove(CustomItemDataComponents.ENCHANTMENT_GLINT_OVERRIDE) {
                        text("Removed enchantment glint override!")
                    }
                }
                thenCallback("skin") {
                    remove(CustomItemDataComponents.SKIN) {
                        text("Removed skin override!")
                    }
                }
            }
            thenCallback("name name", ComponentArgument.textComponent(context())) {
                val name = getArgument<Component>("name")!!
                val item = mainHandItemOrNull() ?: return@thenCallback

                val success = CustomItems.modify(item) {
                    this[CustomItemDataComponents.NAME] = name.wrapWithNotItalic()
                }

                if (success) {
                    text("Renamed item to ") {
                        append(name)
                        append("!")
                    }.sendWithPrefix()
                } else {
                    unableToCustomize()
                }
            }
            then("model") {
                thenCallback("skyblock_model", SkyOceanItemIdArgument()) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val itemId = getArgument<SkyOceanItemId>("skyblock_model")!!

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.MODEL] = SkyblockModel(itemId)

                        if (itemId.toItem() in Items.PLAYER_HEAD) {
                            this[CustomItemDataComponents.SKIN] = SkyblockSkin(itemId)
                        }
                    }

                    if (success) {
                        text("Set item model to ") {
                            append(itemId.id)
                            append("!")
                        }.sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }
                val keys = (McClient.self.modelManager as ModelManagerAccessor).bakedItemModels().keys
                thenCallback("vanilla_model", VirtualResourceArgument(keys)) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val model = getArgument<ResourceLocation>("vanilla_model")!!

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.MODEL] = StaticModel(model)
                    }
                    if (success) {
                        text("Set item model to ") {
                            append(model.toString())
                            append("!")
                        }.sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }
            }
            then("armor_trim material", ResourceKeyArgument(Registries.TRIM_MATERIAL)) {
                thenCallback("pattern", ResourceKeyArgument(Registries.TRIM_PATTERN)) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val material = getArgument<ResourceKey<TrimMaterial>>("material")!!.get()
                    val pattern = getArgument<ResourceKey<TrimPattern>>("pattern")!!.get()

                    val trimMaterial = material?.value() ?: return@thenCallback
                    val trimPattern = pattern?.value() ?: return@thenCallback

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.ARMOR_TRIM] = ArmorTrim(trimMaterial, trimPattern)
                    }
                    if (success) {
                        text("Successfully set armor trim!").sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }
            }
            then("color") {
                thenCallback("hex_color", HexColorArgumentType()) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val color = getArgument<Int>("hex_color")!!

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.COLOR] = StaticItemColor(color)
                    }
                    if (success) {
                        text("Successfully set color to ") {
                            append("#${color.toString(16).padStart(6, '0')}") {
                                this.color = color
                            }
                            append("!")
                        }.sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }

                thenCallback("static_color", SkyOceanItemIdArgument(DyeData.staticDyes.keys.map { SkyOceanItemId.item(it.lowercase()) })) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val color = getArgument<SkyOceanItemId>("static_color")!!

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.COLOR] = SkyBlockDye(color.cleanId)
                    }
                    if (success) {
                        text("Successfully set color to ") {
                            append(color.toItem().hoverName)
                            append("!")
                        }.sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }
                thenCallback("animated_color", SkyOceanItemIdArgument(DyeData.animatedDyes.keys.map { SkyOceanItemId.item(it.lowercase()) })) {
                    val item = mainHandItemOrNull() ?: return@thenCallback
                    val color = getArgument<SkyOceanItemId>("animated_color")!!

                    val success = CustomItems.modify(item) {
                        this[CustomItemDataComponents.COLOR] = AnimatedSkyBlockDye(color.cleanId)
                    }
                    if (success) {
                        text("Successfully set color to ") {
                            append(color.toItem().hoverName)
                            append("!")
                        }.sendWithPrefix()
                    } else {
                        unableToCustomize()
                    }
                }
            }

            thenCallback("enchantment_glint state", BoolArgumentType.bool()) {
                val item = mainHandItemOrNull() ?: return@thenCallback
                val state = getArgument<Boolean>("state")!!

                val success = CustomItems.modify(item) {
                    this[CustomItemDataComponents.ENCHANTMENT_GLINT_OVERRIDE] = state
                }
                if (success) {
                    text("Toggled enchantment glint override ") {
                        if (state) append("on") { this.color = TextColor.GREEN }
                        else append("off") { color = TextColor.RED }
                        append("!")
                    }.sendWithPrefix()
                } else {
                    unableToCustomize()
                }
            }

            thenCallback("skin animated_skull", SkyOceanItemIdArgument(AnimatedSkulls.skins.keys)) {
                val item = mainHandItemOrNull() ?: return@thenCallback
                val skin = getArgument<SkyOceanItemId>("animated_skull")!!

                val success = CustomItems.modify(item) {
                    this[CustomItemDataComponents.SKIN] = AnimatedSkyblockSkin(skin)
                }
                if (success) {
                    text("Successfully set skin texture!").sendWithPrefix()
                } else {
                    unableToCustomize()
                }
            }
        }
    }

    fun remove(type: CustomItemComponent<*>, messageProvider: (item: ItemStack) -> Component) = remove(type, { item, _ -> messageProvider(item) })

    fun <T> remove(type: CustomItemComponent<T>, messageProvider: (item: ItemStack, oldData: T?) -> Component) {
        val item = mainHandItemOrNull() ?: return

        var oldData: T? = null
        val success = CustomItems.modify(item) {
            oldData = this[type]
            this[type] = null
        }

        if (success) {
            messageProvider(item, oldData).sendWithPrefix()
        } else {
            unableToCustomize()
        }
    }

    fun unableToCustomize() {
        text("Unable to customize item!") {
            this.color = OceanColors.WARNING
        }.sendWithPrefix()
    }

    internal fun mainHandItemOrNull(): ItemStack? {
        return McClient.self.player?.mainHandItem?.takeUnless { it.isEmpty } ?: run {
            text("Not holding any item!") {
                this.color = OceanColors.WARNING
            }.sendWithPrefix()
            return null
        }
    }

}
