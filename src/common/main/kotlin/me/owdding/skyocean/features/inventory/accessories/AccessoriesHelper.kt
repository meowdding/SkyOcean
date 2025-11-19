package me.owdding.skyocean.features.inventory.accessories

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.profile.items.accessory.AccessoryBagAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick

@Module
object AccessoriesHelper {

    fun getCurrentIds(): Set<SkyBlockId> = AccessoryBagAPI.getItems().mapNotNullTo(mutableSetOf()) { it.item.getSkyBlockId() }

    @Subscription
    fun onRegisterSkyOceanCommand(event: RegisterSkyOceanCommandEvent) {
        event.register("accessories") {
            thenCallback("missing") {
                val ids = getCurrentIds()
                val missingFamilies = AccessoriesAPI.families.values.filter { family ->
                    family.tiers.none { it.items.any(ids::contains) }
                }

                if (missingFamilies.isEmpty()) {
                    text("You already have all accessories families!").sendWithPrefix()
                } else {
                    text("You are missing ${missingFamilies.size} families! Hover to see.") {
                        hover = Text.join(missingFamilies.map { it.family }, separator = CommonText.NEWLINE)
                        onClick {
                            McClient.clipboard = missingFamilies.joinToString("\n") { it.family }
                            text("Copied missing families to clipboard!").sendWithPrefix()
                        }
                    }.sendWithPrefix()
                }


            }
            thenCallback("upgradeable") {
                val ids = getCurrentIds()
                val upgradeable = ids.filter {
                    val family = AccessoriesAPI.getFamily(it) ?: return@filter false
                    return@filter it !in family.tiers.last()
                }
                if (upgradeable.isEmpty()) {
                    text("You already have all accessories upgraded!").sendWithPrefix()
                } else {
                    val names = upgradeable.map {
                        it.toItem().hoverName
                    }
                    text("You can upgrade ${upgradeable.size} families! Hover to see.") {
                        hover = Text.join(names, separator = CommonText.NEWLINE)
                        onClick {
                            McClient.clipboard = names.joinToString("\n") { it.stripped }
                            text("Copied upgradeable names to clipboard!").sendWithPrefix()
                        }
                    }.sendWithPrefix()
                }
            }
        }
    }

}
