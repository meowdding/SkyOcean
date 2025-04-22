import com.google.gson.JsonObject
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    alias(libs.plugins.loom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.repo)
    alias(libs.plugins.resources)
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
        val mixinPath = configurations.compileClasspath.get().incoming.dependencies.first { it.group == "net.fabricmc" && it.name == "sponge-mixin" }
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
    compileOnly(libs.meowdding.ktmodules)
    ksp(libs.meowdding.ktmodules)

    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment(libs.parchmentmc.get().withMcVersion().toString())
    })

    modImplementation(libs.bundles.fabric)

    implementation(libs.repo) // included in sbapi, exposed through implementation

    includeModImplementationBundle(libs.bundles.sbapi)
    includeModImplementationBundle(libs.bundles.rconfig)
    includeModImplementationBundle(libs.bundles.libs)
    includeModImplementationBundle(libs.bundles.meowdding)

    includeImplementation(libs.keval)

    modRuntimeOnly(libs.devauth)
    modRuntimeOnly(libs.modmenu)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

ksp {
    arg("meowdding.modules.project_name", project.name)
    arg("meowdding.modules.package", "me.owdding.skyocean.generated")
}


compactingResources {
    sourceSets = mutableListOf("client", "main")
    basePath = "repo"
}

repo {
    hotm {
        excludeAllExcept {
            name()
            cost()
        }
        withPredicate {
            when (it) {
                is JsonObject -> it.size() > 1
                else -> true
            }
        }
    }
}

// <editor-fold desc="Util Methods">

fun ExternalModuleDependency.withMcVersion(): ExternalModuleDependency {
    return DefaultMinimalDependency(
        DefaultModuleIdentifier.newId(this.group, this.name.replace("<mc_version>", libs.versions.minecraft.get())),
        DefaultMutableVersionConstraint(this.versionConstraint)
    )
}

@Suppress("unused")
fun DependencyHandlerScope.includeImplementationBundle(bundle: Provider<ExternalModuleDependencyBundle>) = bundle.get().forEach {
    includeImplementation(provider { it })
}

fun DependencyHandlerScope.includeModImplementationBundle(bundle: Provider<ExternalModuleDependencyBundle>) = bundle.get().forEach {
    includeModImplementation(provider { it })
}

fun <T : ExternalModuleDependency> DependencyHandlerScope.includeImplementation(dependencyNotation: Provider<T>) =
    with(dependencyNotation.get().withMcVersion()) {
        include(this)
        modImplementation(this)
    }

fun <T : ExternalModuleDependency> DependencyHandlerScope.includeModImplementation(dependencyNotation: Provider<T>) =
    with(dependencyNotation.get().withMcVersion()) {
        include(this)
        modImplementation(this)
    }
// </editor-fold>
