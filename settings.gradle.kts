enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SkyOcean"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
