@file:Suppress("UnstableApiUsage")

plugins {
    id("net.fabricmc.fabric-loom-remap")
    `ocean-setup`
}

dependencies {
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(versionedCatalog["parchment"]) {
            artifactType("zip")
        })
    })
}
