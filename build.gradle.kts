@file:Suppress("UnstableApiUsage")

import com.google.devtools.ksp.gradle.KspAATask
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.createDirectories

plugins {
    idea
    id("fabric-loom")
    kotlin("jvm") version "2.2.20"
    alias(libs.plugins.kotlin.symbol.processor)
    alias(libs.plugins.meowdding.resources)
    alias(libs.plugins.meowdding.auto.mixins)
    alias(libs.plugins.detekt)
    `versioned-catalogues`
    `museum-data`
}

repositories {
    fun scopedMaven(url: String, vararg paths: String) = maven(url) { content { paths.forEach(::includeGroupAndSubgroups) } }

    scopedMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    scopedMaven("https://repo.hypixel.net/repository/Hypixel", "net.hypixel")
    scopedMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
    scopedMaven("https://api.modrinth.com/maven", "maven.modrinth")
    scopedMaven(
        "https://maven.teamresourceful.com/repository/maven-public/",
        "earth.terrarium",
        "com.teamresourceful",
        "tech.thatgravyboat",
        "me.owdding",
        "com.terraformersmc"
    )
    scopedMaven("https://maven.nucleoid.xyz/", "eu.pb4")
    mavenCentral()
}

configurations {
    modImplementation {
        attributes.attribute(Attribute.of("earth.terrarium.cloche.modLoader", String::class.java), "fabric")
    }
}

dependencies {
    minecraft(versionedCatalog["minecraft"])
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(versionedCatalog["parchment"]) {
            artifactType("zip")
        })
    })

    api(libs.skyblockapi) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}") }
    }
    include(libs.skyblockapi) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}-remapped") }
    }
    api(libs.meowdding.lib) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}") }
    }
    include(libs.meowdding.lib) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}-remapped") }
    }

    includeImplementation(versionedCatalog["resourceful.config"])
    includeImplementation(versionedCatalog["resourceful.lib"])
    includeImplementation(versionedCatalog["placeholders"])
    includeImplementation(versionedCatalog["placeholders"])
    includeImplementation(libs.resourceful.config.kotlin)
    includeImplementation(versionedCatalog["olympus"])

    modRuntimeOnly(libs.hypixel.modapi.fabric)
    implementation(libs.moulberry.mixinconstraints) // Already included in mlib

    implementation(libs.keval)
    include(libs.keval)

    modImplementation(versionedCatalog["fabric.api"])
    modImplementation(libs.fabric.language.kotlin)
    compileOnly(libs.skyblockapi.repo)
    compileOnly(libs.fabric.loader)

    compileOnly(libs.meowdding.ktmodules)
    compileOnly(libs.meowdding.ktcodecs)

    ksp(libs.meowdding.ktmodules)
    ksp(libs.meowdding.ktcodecs)

    detektPlugins(libs.detekt.ktlintWrapper)
}

fun DependencyHandler.includeImplementation(dep: Any) {
    include(dep)
    modImplementation(dep)
}

val mcVersion = stonecutter.current.version.replace(".", "")
val accessWidenerFile = rootProject.file("src/skyocean.accesswidener")
loom {
    runConfigs["client"].apply {
        ideConfigGenerated(true)
        runDir = "../../run"
        vmArg("-Dfabric.modsFolder=" + '"' + "${mcVersion}Mods" + '"')
    }

    if (accessWidenerFile.exists()) {
        accessWidenerPath.set(accessWidenerFile)
    }
}

val datagenOutput = project.layout.buildDirectory.file("generated/skyocean/data").apply {
    get().asFile.toPath().createDirectories()
}
fabricApi {
    configureDataGeneration {
        client = true
        modId = "skyocean-datagen"
        createSourceSet = true
        createRunConfiguration = true
        outputDirectory.set(datagenOutput)
    }
}

ksp {
    arg("meowdding.package", "me.owdding.skyocean.generated")
}

afterEvaluate {
    loom {
        log4jConfigs.removeAll { true }
        log4jConfigs.from(rootProject.layout.projectDirectory.file("gradle/log4j.config.xml"))

        runs.named("datagen") {
            this.vmArgs.add("-Dskyocean.extraPaths=\"\"")
        }
    }

    tasks.withType(KspAATask::class.java).configureEach {
        kspConfig.processorOptions.put(
            "meowdding.project_name",
            "SkyOcean" + (kspConfig.cachesDir.get().asFile.name.takeUnless { it == "main" }?.replaceFirstChar { it.uppercaseChar() } ?: "")
        )
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

val archiveName = "SkyOcean"

base {
    archivesName.set("$archiveName-${archivesName.get()}")
}

tasks.named("build") {
    doLast {
        val sourceFile = rootProject.projectDir.resolve("versions/${project.name}/build/libs/${archiveName}-${stonecutter.current.version}-$version.jar")
        val targetFile = rootProject.projectDir.resolve("build/libs/${archiveName}-$version-${stonecutter.current.version}.jar")
        targetFile.parentFile.mkdirs()
        targetFile.writeBytes(sourceFile.readBytes())
    }
}

compactingResources {
    basePath = "repo"
    pathDirectory = "../../src"

    configureTask(tasks.named<AbstractCopyTask>("processResources").get())

    removeComments("unobtainable_ids")
    downloadResource(
        "https://raw.githubusercontent.com/Campionnn/SkyShards-Parser/55483450ff83e1bf1e453f31797cedb08b0c2733/shard-data.json",
        "skyshards_data.json"
    )
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
    compilerOptions.freeCompilerArgs.addAll(
        "-Xcontext-parameters",
        "-Xcontext-sensitive-resolution",
        "-Xnullability-annotations=@org.jspecify.annotations:warn"
    )
}

tasks.processResources {
    val replacements = mapOf(
        "version" to version,
        "minecraft_start" to versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft"),
        "minecraft_end" to versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft"),
        "fabric_lang_kotlin" to libs.versions.fabric.language.kotlin.get(),
        "sbapi" to libs.versions.skyblockapi.asProvider().get(),
        "rlib" to versionedCatalog.versions["resourceful.lib"],
        "olympus" to versionedCatalog.versions["olympus"],
        "mlib" to libs.versions.meowdding.lib.get(),
        "rconfigkt" to libs.versions.rconfigkt.get(),
        "rconfig" to versionedCatalog.versions["resourceful.config"],
        "placeholder_api" to versionedCatalog.versions["placeholders"]
    )
    inputs.properties(replacements)

    filesMatching("fabric.mod.json") {
        expand(replacements)
    }
}

autoMixins {
    mixinPackage = "me.owdding.skyocean.mixins"
    projectName = "skyocean"
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching(listOf("**/*.fsh", "**/*.vsh")) {
        // `#` is used for all versions, `!` is used for multiversioned imports
        filter { if (it.startsWith("//#moj_import") || it.startsWith("//!moj_import")) "#${it.substring(3)}" else it }
    }
    with(copySpec {
        from(rootProject.file("src/lang")).include("*.json").into("assets/skyocean/lang")
    })
    with(copySpec {
        from(accessWidenerFile)
    })
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        excludeDirs.add(file("run"))
    }
}

tasks.withType<ValidateAccessWidenerTask> { enabled = false }

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

detekt {
    source.setFrom(project.sourceSets.map { it.allSource })
    config.from(files("$rootDir/detekt/detekt.yml"))
    baseline = file("$rootDir/detekt/${project.name}-baseline.xml")
    buildUponDefaultConfig = true
    parallel = true
}

tasks.named { it == "jar" || it == "sourcesJar" }.configureEach {
    if (this !is Jar) return@configureEach
    if (rootProject.hasProperty("datagen")) {
        dependsOn(tasks.named("runDatagen"))
        with(copySpec {
            from(datagenOutput).exclude(".cache/**")
        })
    }
}

tasks.withType<Detekt>().configureEach {
    onlyIf {
        !rootProject.hasProperty("skipDetekt")
    }
    exclude { it.file.toPath().toAbsolutePath().startsWith(project.layout.buildDirectory.get().asFile.toPath()) }
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    exclude { it.file.toPath().toAbsolutePath().startsWith(project.layout.buildDirectory.get().asFile.toPath()) }
    outputs.upToDateWhen { false }
}
