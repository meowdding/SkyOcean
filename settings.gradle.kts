enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("detekt")

rootProject.name = "SkyOcean"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
        maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
