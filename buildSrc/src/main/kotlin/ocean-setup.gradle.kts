import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import net.fabricmc.loom.task.ValidateAccessWidenerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.createDirectories

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("me.owdding.auto-mixins")
    id("me.owdding.resources")
    id("idea")
    id("dev.detekt")
    id("versioned-catalogues")
    id("museum-data")
}

private val stonecutter = project.extensions.getByName("stonecutter") as dev.kikugie.stonecutter.build.StonecutterBuildExtension
fun isUnobfuscated() = stonecutter.eval(stonecutter.current.version, ">=26.1")

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

fun makeAlias(configuration: String) = if (isUnobfuscated()) configuration else "mod" + configuration.replaceFirstChar { it.uppercase() }

val maybeModImplementation = makeAlias("implementation")
val maybeModCompileOnly = makeAlias("compileOnly")
val maybeModRuntimeOnly = makeAlias("runtimeOnly")
val maybeModApi = makeAlias("api")


tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(if (isUnobfuscated()) 25 else 21)
}

kotlin {
    jvmToolchain(if (isUnobfuscated()) 25 else 21)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(if (isUnobfuscated()) 25 else 21)
    withSourcesJar()
}


tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(if (isUnobfuscated()) JvmTarget.JVM_25 else JvmTarget.JVM_21)
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
    compilerOptions.freeCompilerArgs.addAll(
        "-Xcontext-parameters",
        "-Xcontext-sensitive-resolution",
        "-Xnullability-annotations=@org.jspecify.annotations:warn"
    )
}


fun DependencyHandlerScope.includeImplementation(dep: Any) {
    "include"(dep)
    maybeModImplementation(dep)
}

val mcVersion = stonecutter.current.version.replace(".", "")
val accessWidenerFile = rootProject.file(if (isUnobfuscated()) "src/skyocean.accesswidener" else "src/skyocean.obf.accesswidener")

val loom = extensions.getByName<LoomGradleExtensionAPI>("loom")
loom.apply {
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

extensions.getByName<FabricApiExtension>("fabricApi").apply {
    configureDataGeneration {
        client = true
        modId = "skyocean-datagen"
        createSourceSet = true
        createRunConfiguration = true
        outputDirectory.set(datagenOutput)
    }
}

extensions.getByType<KspExtension>().apply {
    arg("meowdding.package", "me.owdding.skyocean.generated")
}

afterEvaluate {
    loom.apply {
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

    removeComments("vanguard") // when does this just auto remove comments from all .jsoncs, why do i need to specify this
    removeComments("unobtainable_ids")
    downloadResource(
        "https://raw.githubusercontent.com/Campionnn/SkyShards-Parser/55483450ff83e1bf1e453f31797cedb08b0c2733/shard-data.json",
        "skyshards_data.json"
    )
}


tasks.processResources {
    val range = if (versionedCatalog.versions.has("minecraft.range")) {
        versionedCatalog.versions.get("minecraft.range").toString()
    } else {
        val start = versionedCatalog.versions.getOrFallback("minecraft.start", "minecraft")
        val end = versionedCatalog.versions.getOrFallback("minecraft.end", "minecraft")
        ">=$start <=$end"
    }
    val replacements = mapOf(
        "version" to version,
        "minecraft_range" to range,
        "fabric_lang_kotlin" to versionedCatalog.versions["fabric.language.kotlin"],
        "sbapi" to versionedCatalog.versions["skyblockapi"],
        "rlib" to versionedCatalog.versions["resourceful.lib"],
        "olympus" to versionedCatalog.versions["olympus"],
        "mlib" to versionedCatalog.versions["meowdding.lib"],
        "rconfigkt" to versionedCatalog.versions["rconfigkt"],
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
    mixinExtrasVersion = "0.5.0"
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
        rename { it.replace(".obf", "") }
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

dependencies {
    "minecraft"(versionedCatalog["minecraft"])

    api(versionedCatalog["skyblockapi"]) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}") }
    }
    "include"(versionedCatalog["skyblockapi"]) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-${stonecutter.current.version}${"-remapped".takeIf { !isUnobfuscated() } ?: ""}") }
    }
    api(versionedCatalog["meowdding.lib"]) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}") }
    }
    "include"(versionedCatalog["meowdding.lib"]) {
        capabilities { requireCapability("me.owdding.meowdding-lib:meowdding-lib-${stonecutter.current.version}${"-remapped".takeIf { !isUnobfuscated() } ?: ""}") }
    }

    includeImplementation(versionedCatalog["resourceful.config"])
    includeImplementation(versionedCatalog["resourceful.lib"])
    includeImplementation(versionedCatalog["placeholders"])
    includeImplementation(versionedCatalog["placeholders"])
    includeImplementation(versionedCatalog["resourceful.config.kotlin"])
    includeImplementation(versionedCatalog["olympus"])

    implementation(versionedCatalog["moulberry.mixinconstraints"]) // Already included in mlib

    implementation(versionedCatalog["keval"])
    "include"(versionedCatalog["keval"])

    maybeModImplementation(versionedCatalog["fabric.api"])
    maybeModRuntimeOnly(versionedCatalog["fabric.language.kotlin"])
    compileOnly(versionedCatalog["skyblockapi.repo"])
    compileOnly(versionedCatalog["fabric.loader"])

    compileOnly(versionedCatalog["meowdding.ktmodules"])
    compileOnly(versionedCatalog["meowdding.ktcodecs"])

    ksp(versionedCatalog["meowdding.ktmodules"])
    ksp(versionedCatalog["meowdding.ktcodecs"])

    detektPlugins(versionedCatalog["detekt.ktlintWrapper"])
}
