import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

val resourcesDir: String = "src/main/resources"

plugins {
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "1.3.24"
}

/*
 * Load project properties
 */

class Props {
    private fun propS(propName: String): String = providers.gradleProperty(propName).get()

    private fun optS(propName: String): String? = providers.gradleProperty(propName).orNull

    private fun propSL(propName: String): List<String> = providers.gradleProperty(propName)
        .map { strVal -> strVal.split(',').map { it.trim() } }
        .getOrElse(listOf())

    private fun propB(propName: String): Boolean = providers.gradleProperty(propName).map {
        when (val strVal = it.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Not a boolean value: $strVal")
        }
    }.get()

    val modId: String = propS("mod.id")
    val modPackage: String = propS("mod.package")
    val modVersion: String = propS("mod.version")

    val modATs: String? = optS("mod.access_transformers")
    val modConstClass: String = propS("mod.const_class")
    val modLoadingPlugin: String? = optS("mod.loading_plugin")?.let { "$modPackage.$it" }

    val mcVersion: String = propS("minecraft.version")
    val mcDeobfMappingsChannel: String = propS("minecraft.deobf.mappings.channel")
    val mcDeobfMappingsVersion: String = propS("minecraft.deobf.mappings.version")

    val buildSources: Boolean = propB("build.buildSources")
    val buildDocs: Boolean = propB("build.buildDocs")
}

val props = Props()

inline fun <T> withProps(f: Props.() -> T): T = props.run(f)

/*
 * Configure project
 */

group = props.modPackage
version = props.modVersion

base {
    archivesName = withProps { "$modId-$mcVersion" }
}

/*
 * Configure build system
 */

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.AZUL // RFG requires an Azul JVM for its build tasks
    }

    if (props.buildSources) {
        withSourcesJar()
    }
    if (props.buildDocs) {
        withJavadocJar()
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

minecraft {
    // Build config
    mcVersion = props.mcVersion
    mcpMappingChannel = props.mcDeobfMappingsChannel
    mcpMappingVersion = props.mcDeobfMappingsVersion

    useDependencyAccessTransformers = true

    injectedTags.apply {
        put("MOD_ID", props.modId)
        put("VERSION", props.modVersion)
    }

    // Runtime config
    username = "Player"
    extraRunJvmArguments.add("-ea:${props.modPackage}")
    props.modLoadingPlugin?.let { extraRunJvmArguments.add("-Dfml.coreMods.load=$it") }
}

tasks.injectTags {
    outputClassName = withProps { "$modPackage.$modConstClass" }
}

tasks.processResources {
    inputs.property("version", props.modVersion)

    filesMatching("mcmod.info") {
        expand(
            "modId" to props.modId,
            "modVersion" to props.modVersion,
            "mcVersion" to props.mcVersion
        )
    }
}

tasks.deobfuscateMergedJarToSrg {
    props.modATs?.let { accessTransformerFiles.from("$resourcesDir/META-INF/$it") }
}

tasks.srgifyBinpatchedJar {
    props.modATs?.let { accessTransformerFiles.from("$resourcesDir/META-INF/$it") }
}

tasks.jar {
    manifest {
        props.modATs?.let { attributes("FMLAT" to it) }
        props.modLoadingPlugin?.let {
            attributes("FMLCorePlugin" to it, "FMLCorePluginContainsFMLMod" to "true")
        }
    }
}

/*
 * Configure artifact publication
 */

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = withProps { "$modId-$mcVersion" }
            from(components["java"])
        }
    }
}

/*
 * Configure IDE integration
 */

eclipse {
    classpath {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        inheritOutputDirs = true // Fix resources in IJ-Native runs
    }
    project {
        settings {
            runConfigurations {
                add(Gradle("1. Run Client").apply {
                    setProperty("taskNames", listOf("runClient"))
                })
                add(Gradle("2. Run Server").apply {
                    setProperty("taskNames", listOf("runServer"))
                })
                add(Gradle("3. Run Obfuscated Client").apply {
                    setProperty("taskNames", listOf("runObfClient"))
                })
                add(Gradle("4. Run Obfuscated Server").apply {
                    setProperty("taskNames", listOf("runObfServer"))
                })
            }
            compiler {
                afterEvaluate {
                    javac {
                        javacAdditionalOptions = "-encoding utf8"
                        moduleJavacAdditionalOptions = mapOf(
                            (project.name + ".main") to
                                tasks.compileJava.get().options.compilerArgs.joinToString(" ") { "\"$it\"" }
                        )
                    }
                }
            }
        }
    }
}

tasks.processIdeaSettings {
    dependsOn(tasks.injectTags)
}
