package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.vault.VaultAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object VaultItemSource : ItemSource {
    override fun getAll() = VaultAPI.getItems().map { SimpleTrackedItem(it, VaultItemContext) }

    override val type = ItemSources.VAULT
}

object VaultItemContext : ItemContext {
    override val source = ItemSources.VAULT
    override fun collectLines() = build {
        add("Vault") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open bank!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(sendMessage = true) {
        requiresCookie(allowOnBingo = true) {
            McClient.sendCommand("/bank")
        }
    }
}
