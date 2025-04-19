import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version "2.1.0"
    alias(libs.plugins.loom)
    id("maven-publish")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

base {
    archivesName.set(project.name.lowercase())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    accessWidenerPath.set(project.projectDir.resolve("src/main/resources/skyocean.accesswidener"))

    runs {
        getByName("client") {
            programArg("--quickPlayMultiplayer=hypixel.net")
            vmArg("-Ddevauth.enabled=true")
            vmArg("-Dskyblockapi.debug=true")
        }
        afterEvaluate {
            getByName("datagen") {
                vmArg("-Ddevauth.enabled=false")
            }
        }
    }

    mods {
        register("skyocean") {
            sourceSet("client")
            sourceSet("main")
        }
    }

    afterEvaluate {
        val mixinPath = configurations.compileClasspath.get()
            .files { it.group == "net.fabricmc" && it.name == "sponge-mixin" }
            .first()
        runConfigs {
            "client" {
                vmArgs.add("-javaagent:$mixinPath")
            }
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
        createSourceSet = true
        addToResources = true
        outputDirectory.set(project.layout.buildDirectory.dir("generated/datagen").get().asFile)
    }
}

tasks.getByName("sourcesJar").apply {
    dependsOn(tasks.getByName("runDatagen"))
}

repositories {
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    mavenLocal()
}

dependencies {
    compileOnly(ksp(project(":annotations"))!!)

    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.3:2024.12.07@zip")
    })
    modImplementation(libs.loader)
    modImplementation(libs.fabrickotlin)
    modImplementation(libs.fabric)

    modImplementation(libs.hypixelapi)
    modImplementation(libs.skyblockapi)
    modImplementation(libs.rconfig)
    modImplementation(libs.rconfigkt) {
        isTransitive = false
    }
    modImplementation(libs.rlib)
    modImplementation(libs.olympus)
    modImplementation(libs.placeholders)
    modImplementation(libs.repo)
    modImplementation(libs.meowdding.lib)

    include(libs.hypixelapi)
    include(libs.skyblockapi)
    include(libs.rconfig)
    include(libs.rconfigkt) {
        isTransitive = false
    }
    include(libs.rlib)
    include(libs.olympus)
    include(libs.placeholders)
    include(libs.repo)
    include(libs.meowdding.lib)

    modRuntimeOnly(libs.devauth)
    modRuntimeOnly(libs.modmenu)
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching(listOf("fabric.mod.json")) {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

afterEvaluate {
    tasks.getByName("kspDatagenKotlin") {
        enabled = false
    }
}
