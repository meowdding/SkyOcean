@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalPathApi::class)

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import earth.terrarium.cloche.api.metadata.ModMetadata
import earth.terrarium.cloche.api.target.compilation.ClocheDependencyHandler
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import me.owdding.repo.toPath
import net.msrandom.minecraftcodev.core.utils.toPath
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.runs.task.WriteClasspathFile
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
    maven(url = "https://repo.hypixel.net/repository/Hypixel/")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven(url = "https://maven.nucleoid.xyz")
    maven(url = "https://maven.shedaniel.me/")
    maven(url = "https://maven.msrandom.net/repository/root")
    maven(url = "https://maven.notenoughupdates.org/releases") // Needed for detekt rules
    mavenLocal()
}

dependencies {
    compileOnly(libs.meowdding.ktmodules)
    ksp(libs.meowdding.ktmodules)
    compileOnly(libs.meowdding.ktcodecs)
    ksp(libs.meowdding.ktcodecs)

    compileOnly(libs.keval)
    compileOnly(libs.kotlin.stdlib)


    detektPlugins("org.notenoughupdates:detektrules:1.0.0")
    detektPlugins(project(":detekt"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
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
        compileOnly(libs.meowdding.ktcodecs)
        compileOnly(libs.meowdding.ktmodules)

        modImplementation(libs.meowdding.lib)
        modImplementation(libs.skyblockapi)
        compileOnly(libs.skyblockapi.repo)
        implementation(libs.keval)
        modImplementation(libs.placeholders)
        modImplementation(libs.resourceful.config.kotlin) { isTransitive = false }

        modImplementation(libs.fabric.language.kotlin)
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
                entrypoint("client") {
                    adapter = "kotlin"
                    value = "me.owdding.skyocean.SkyOcean"
                }
                entrypoint("fabric-datagen") {
                    adapter = "kotlin"
                    value = "me.owdding.skyocean.datagen.SkyOceanDatagen"
                }

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

            data {
                includedClient()
            }

            dependencies {
                fabricApi(fabricApiVersion, minecraftVersion)
                modImplementation(olympus)
                modImplementation(rconfig)

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

            val sourceSetName = this.sourceSet.name

            tasks {
                afterEvaluate {
                    val datagen = getByName("run${sourceSetName}ClientData")
                    val processResources = getByName("process${sourceSetName}Resources", ProcessResources::class)
                    val postProcessResources = register("postProcess${sourceSetName}Resources", ProcessResources::class) {
                        dependsOn(processResources)
                        mustRunAfter(datagen)
                        inputs.files(processResources.inputs.files)
                        actions.addAll(processResources.actions)
                        outputs.upToDateWhen { false }
                        destinationDir = processResources.destinationDir
                        with(processResources.rootSpec)
                    }

                    getByName("${sourceSetName}Jar") {
                        dependsOn("run${sourceSetName}ClientData")
                        dependsOn(postProcessResources)
                    }
                }
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

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    config.setFrom(rootProject.layout.projectDirectory.file("detekt/detekt.yml")) // point to your custom config defining rules to run, overwriting default behavior
    baseline = file(layout.projectDirectory.file("detekt/baseline.xml")) // a way of suppressing issues before introducing detekt
    source.setFrom(project.sourceSets.map { it.allSource })
}

tasks.withType<Detekt>().configureEach {
    onlyIf {
        project.findProperty("skipDetekt") != "true"
    }
    exclude { it.file.toPath().toAbsolutePath().startsWith(project.layout.buildDirectory.toPath()) }
    outputs.cacheIf { false } // Custom rules won't work if cached
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    exclude { it.file.toPath().toAbsolutePath().startsWith(project.layout.buildDirectory.toPath()) }
    outputs.cacheIf { false } // Custom rules won't work if cached
    outputs.upToDateWhen { false }
}

compactingResources {
    basePath = "repo"

    configureTask(tasks.getByName<ProcessResources>("process1218Resources"))
    configureTask(tasks.getByName<ProcessResources>("process1215Resources"))
    configureTask(tasks.getByName<ProcessResources>("processResources"))

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

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching(listOf("**/*.fsh", "**/*.vsh")) {
        filter { if (it.startsWith("//!moj_import")) "#${it.substring(3)}" else it }
    }
    exclude(".cache/**")

    with(copySpec {
        from("src/lang").include("*.json").into("assets/skyocean/lang")
    })
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
        freeCompilerArgs.addAll(
            "-Xmulti-platform",
            "-Xno-check-actual",
            "-Xexpect-actual-classes",
        )
    }
}

ksp {
    sourceSets.filterNot { it.name == SourceSet.MAIN_SOURCE_SET_NAME }.forEach { this.excludedSources.from(it.kotlin.srcDirs) }
    arg("meowdding.project_name", project.name)
    arg("meowdding.package", "me.owdding.skyocean.generated")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}


afterEvaluate {
    tasks.named("createCommonApiStub", GenerateStubApi::class).configure {
        excludes.add(libs.skyblockapi.asProvider().get().module.toString())
        excludes.add(libs.meowdding.lib.get().module.toString())
    }
}

// TODO temporary workaround for a cloche issue on certain systems, remove once fixed
tasks.withType<WriteClasspathFile>().configureEach {
    actions.clear()
    actions.add {
        output.get().toPath().also { it.parent.createDirectories() }.takeUnless { it.exists() }?.createFile()
        generate()
        val file = output.get().toPath()
        file.writeText(file.readText().lines().joinToString(File.pathSeparator))
    }
}

val mcVersions = sourceSets.filterNot { it.name == SourceSet.MAIN_SOURCE_SET_NAME || it.name == SourceSet.TEST_SOURCE_SET_NAME }.map { it.name }

tasks.register("release") {
    group = "meowdding"
    mcVersions.forEach {
        tasks.findByName("${it}JarInJar")?.let { task ->
            dependsOn(task)
            mustRunAfter(task)
        }
    }
}

tasks.register("cleanRelease") {
    group = "meowdding"
    listOf("clean", "release").forEach {
        tasks.getByName(it).let { task ->
            dependsOn(task)
            mustRunAfter(task)
        }
    }
}

tasks.register("setupForWorkflows") {
    mcVersions.flatMap {
        listOf("remap${it}CommonMinecraftNamed", "remap${it}ClientMinecraftNamed")
    }.mapNotNull { tasks.findByName(it) }.forEach {
        dependsOn(it)
        mustRunAfter(it)
    }
}

tasks.withType<JarInJar>().configureEach {
    include { !it.name.endsWith("-dev.jar") }

    manifest {
        attributes["Fabric-Loom-Mixin-Remap-Type"] = "static"
        attributes["Fabric-Jar-Type"] = "classes"
        attributes["Fabric-Mapping-Namespace"] = "intermediary"
    }
}
