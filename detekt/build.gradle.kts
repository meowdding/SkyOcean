import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

// Dependencies:
repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/maven/")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://repo.nea.moe/releases")
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    implementation("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.7")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:1.23.7")
}

tasks.withType<Detekt>().configureEach {
    onlyIf { false }
}
