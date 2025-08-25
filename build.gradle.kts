@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalPathApi::class)

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.metadata.ModMetadata
import earth.terrarium.cloche.api.target.compilation.ClocheDependencyHandler
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.runs.MinecraftRunConfiguration
import net.msrandom.stubs.GenerateStubApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile
import kotlin.io.path.*

plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.terrarium.cloche)
    alias(libs.plugins.meowdding.resources)
    alias(libs.plugins.meowdding.repo)
    alias(libs.plugins.kotlin.symbol.processor)
    alias(libs.plugins.detekt)
    alias(libs.plugins.meowdding.gradle)
}

base {
    archivesName.set(project.name.lowercase())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    maven(url = "https://maven.shedaniel.me/")
    maven(url = "https://maven.msrandom.net/repository/root")
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(libs.keval)
    compileOnly(libs.kotlin.stdlib)

    detektPlugins(project(":detekt"))
}

cloche {
    metadata {
        modId = "skyocean"
        name = "SkyOcean"
        icon = "assets/skyocean/skyocean-big.png"
        description = "SkyOcean is a hypixel skyblock mod that aims to provide a better playing experience by integrating QOL elements in an unnoticeable way."
        license = "MIT"
        clientOnly = true
    }

    fun addDependencies(arg: ClocheDependencyHandler) = arg.apply {
        implementation(libs.meowdding.lib)
        implementation(libs.skyblockapi)
        compileOnly(libs.skyblockapi.repo)
        implementation(libs.keval)
        implementation(libs.placeholders)
        implementation(libs.resourceful.config.kotlin) { isTransitive = false }

        implementation(libs.fabric.language.kotlin)
    }

    common {
        project.layout.projectDirectory.dir("src/mixins").toPath().listDirectoryEntries().filter { it.isRegularFile() }.forEach {
            mixins.from("src/mixins/${it.name}")
        }

        data {
            dependencies { addDependencies(this) }
        }
        dependencies { addDependencies(this) }
    }

    fun createVersion(
        name: String,
        version: String = name,
        loaderVersion: Provider<String> = libs.versions.fabric.loader,
        fabricApiVersion: Provider<String> = libs.versions.fabric.api,
        minecraftVersionRange: ModMetadata.VersionRange.() -> Unit = {
            start = version
            end = version
            endExclusive = false
        },
        dependencies: MutableMap<String, Provider<MinimalExternalModuleDependency>>.() -> Unit = { },
    ) {
        val dependencies = mutableMapOf<String, Provider<MinimalExternalModuleDependency>>().apply(dependencies)
        val olympus = dependencies["olympus"]!!
        val rlib = dependencies["resourcefullib"]!!
        val rconfig = dependencies["resourcefulconfig"]!!

        fabric(name) {
            includedClient()
            minecraftVersion = version
            this.loaderVersion = loaderVersion.get()

            mixins.from("src/mixins/versioned/skyocean.${sourceSet.name}.mixins.json")

            // include(libs.hypixelapi) - included in sbapi
            include(libs.skyblockapi)
            include(libs.resourceful.config.kotlin)
            include(libs.meowdding.lib)
            include(libs.keval)
            include(libs.placeholders)
            include(rlib)
            include(olympus)
            include(rconfig)

            metadata {
                fun kotlin(value: String): Action<FabricMetadata.Entrypoint> = Action {
                    adapter = "kotlin"
                    this.value = value
                }
                entrypoint("client") {
                    adapter = "kotlin"
                    value = "me.owdding.skyocean.SkyOcean"
                }
                entrypoint("fabric-datagen") {
                    adapter = "kotlin"
                    value = ""
                }
                entrypoint(
                    "fabric-datagen", listOf(
                        kotlin("me.owdding.skyocean.datagen.SkyOceanDatagen"),
                        kotlin("me.owdding.skyocean.datagen.resourcepacks.SkyOceanDeepHollows"),
                    )
                )

                fun dependency(modId: String, version: Provider<String>? = null) {
                    dependency {
                        this.modId = modId
                        this.required = true
                        if (version != null) version {
                            this.start = version
                        }
                    }
                }

                dependency {
                    modId = "minecraft"
                    required = true
                    version(minecraftVersionRange)
                }
                dependency("fabric")
                dependency("fabricloader", loaderVersion)
                dependency("fabric-language-kotlin", libs.versions.fabric.language.kotlin)
                dependency("resourcefullib", rlib.map { it.version!! })
                dependency("skyblock-api", libs.versions.skyblockapi.asProvider())
                dependency("olympus", olympus.map { it.version!! })
                dependency("placeholder-api", libs.versions.placeholders)
                dependency("resourcefulconfigkt", libs.versions.rconfigkt)
                dependency("resourcefulconfig", rconfig.map { it.version!! })
                dependency("meowdding-lib", libs.versions.meowdding.lib)
            }

            data()

            dependencies {
                fabricApi(fabricApiVersion, minecraftVersion)
                implementation(olympus)
                implementation(rconfig)

                val mods = project.layout.buildDirectory.get().toPath().resolve("tmp/extracted${sourceSet.name}RuntimeMods")
                val modsTmp = project.layout.buildDirectory.get().toPath().resolve("tmp/extracted${sourceSet.name}RuntimeMods/tmp")

                mods.deleteRecursively()
                modsTmp.createDirectories()
                mods.createDirectories()

                fun extractMods(file: java.nio.file.Path) {
                    println("Adding runtime mod ${file.name}")
                    val extracted = mods.resolve(file.name)
                    file.copyTo(extracted, overwrite = true)
                    if (!file.fileName.endsWith(".disabled.jar")) {
                        modRuntimeOnly(files(extracted))
                    }
                    ZipFile(extracted.toFile()).use {
                        it.entries().asIterator().forEach { file ->
                            val name = file.name.replace(File.separator, "/")
                            if (name.startsWith("META-INF/jars/") && name.endsWith(".jar")) {
                                val data = it.getInputStream(file).readAllBytes()
                                val file = modsTmp.resolve(name.substringAfterLast("/"))
                                file.writeBytes(data, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                                extractMods(file)
                            }
                        }
                    }
                }

                project.layout.projectDirectory.toPath().resolve("run/${sourceSet.name}Mods").takeIf { it.exists() }
                    ?.listDirectoryEntries()?.filter { it.isRegularFile() }?.forEach { file ->
                        extractMods(file)
                    }

                modsTmp.deleteRecursively()
            }

            runs {
                clientData {
                    mainClass("net.fabricmc.loader.impl.launch.knot.KnotClient")
                }
                client()
            }

        }
    }

    createVersion("1.21.5", fabricApiVersion = provider { "0.127.1" }) {
        this["resourcefullib"] = libs.resourceful.lib1215
        this["resourcefulconfig"] = libs.resourceful.config1215
        this["olympus"] = libs.olympus.lib1215
    }
    createVersion("1.21.8", minecraftVersionRange = {
        start = "1.21.6"
    }) {
        this["resourcefullib"] = libs.resourceful.lib1218
        this["resourcefulconfig"] = libs.resourceful.config1218
        this["olympus"] = libs.olympus.lib1218
    }

    mappings { official() }
}

compactingResources {
    basePath = "repo"

    tasks.withType<ProcessResources> {
        configureTask(this)
    }

    compactToArray("recipes")
}

repo {
    val predicate: (JsonElement) -> Boolean = {
        when (it) {
            is JsonObject -> it.size() > 1
            else -> true
        }
    }
    hotm {
        excludeAllExcept {
            name()
            cost()
        }
        withPredicate(predicate)
    }
    hotf {
        excludeAllExcept {
            name()
            cost()
        }
        withPredicate(predicate)
    }
    sacks { includeAll() }
}

tasks {
    withType<ProcessResources>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        compilerOptions {
            languageVersion = KotlinVersion.KOTLIN_2_2
            freeCompilerArgs.addAll(
                "-Xmulti-platform",
                "-Xno-check-actual",
                "-Xexpect-actual-classes",
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xcontext-parameters",
            )
        }
    }
}

val createResourcePacks = tasks.register("createResourcePacks").apply {
    configure {
        group = "meowdding"
    }
}

cloche.targets.forEach { target ->
    target.runs {
        val run = this
        run.clientData.takeIf { clientData -> clientData.value.isPresent }?.configure {
            val parentRun = this
            minecraftRuns.add(objects.newInstance(MinecraftRunConfiguration::class, "${parentRun.name}:resourcepack", project).apply {
                this.mainClass = "me.owdding.skyocean.datagen.dispatcher.SkyOceanDatagenDispatcher"
                this.jvmVersion = parentRun.jvmVersion
                this.sourceSet = parentRun.sourceSet
                this.beforeRun = parentRun.beforeRun
                this.arguments = parentRun.arguments
                this.jvmArguments = parentRun.jvmArguments
                this.environment = parentRun.environment
                this.workingDirectory = parentRun.workingDirectory
                jvmArgs("-Dskyocean.datagen.target=RESOURCE_PACKS")
                jvmArgs("-Dskyocean.datagen.dir=${project.layout.buildDirectory.dir("resourcepacks/${target.name}").get().toPath().absolutePathString()}")
                jvmArgs("-Dskyocean.datagen.output=${project.layout.buildDirectory.dir("libs").get().toPath().absolutePathString()}")
                createResourcePacks.get().dependsOn(this.runTask)
                createResourcePacks.get().mustRunAfter(this.runTask)
            })
        }
    }
}

ksp {
    arg("actualStubDir", project.layout.buildDirectory.dir("generated/ksp/main/stubs").get().asFile.absolutePath)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

afterEvaluate {
    tasks.withType<GenerateStubApi> {
        excludes.addAll(
            "org.jetbrains.kotlin",
            "me.owdding",
            "net.hypixel",
            "maven.modrinth",
            "com.fasterxml.jackson",
            "com.google",
            "com.ibm",
            "io.netty",
            "net.fabricmc:fabric-language-kotlin",
            "com.mojang:datafixerupper",
            "com.mojang:brigardier",
            "io.github.llamalad7:mixinextras",
            "net.minidev",
            "com.nimbusds",
            "tech.thatgravyboat",
            "net.msrandom",
            "eu.pb4"
        )
    }
}

meowdding {
    setupClocheClasspathFix()
    configureModules = true
    configureCodecs = true
    configureDetekt = true
}
