package me.owdding.skyocean.features.dungeons.gambling.chest

enum class DungeonChestType {
    WOODEN,
    GOLD,
    DIAMOND,
    EMERALD,
    OBSIDIAN,
    BEDROCK,
    ;

    companion object {
        fun getByName(name: String): DungeonChestType? = entries.find { it.name.equals(name, true) }
        fun getByNameStartsWith(name: String): DungeonChestType? = entries.find { name.startsWith(it.name, true) }
    }
}
