package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.vault.VaultAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object VaultItemSource : ItemSource {
    override fun getAll() = VaultAPI.getItems().map { SimpleTrackedItem(it, VaultItemContex) }

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.VAULT
}

object VaultItemContex : ItemContext {
    override val source = ItemSources.VAULT
    override fun collectLines() = build {
        add("Vault") { color = TextColor.GRAY }
    }

    override fun open() = requiresCookie { McClient.sendCommand("/bank") }
}
