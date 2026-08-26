import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    idea
    `java-library`
    `maven-publish`
    id("license-conventions")
}

val stonecutterGenerateTask = ":common:${commonMod.prop("minecraft_version")}:stonecutterGenerate"
val licenseFormat = tasks.named("licenseFormat") {
    dependsOn(stonecutterGenerateTask)
}

val configuredJavaVersion = commonMod.mc.let { version ->
    val parts = version.split('.').mapNotNull(String::toIntOrNull)
    val major = parts.getOrElse(0) { 0 }
    val minor = parts.getOrElse(1) { 0 }
    val patch = parts.getOrElse(2) { 0 }

    when {
        major >= 26 -> 25
        major == 1 && (minor > 20 || (minor == 20 && patch >= 5)) -> 21
        major == 1 && minor >= 18 -> 17
        major == 1 && minor >= 17 -> 16
        else -> 8
    }
}
val mixinCompatibilityLevel = "JAVA_$configuredJavaVersion"
val buildSuffix = commonMod.propOrNull("build.number")?.trim()?.takeIf(String::isNotEmpty)
version = listOfNotNull(commonMod.version, buildSuffix, "mc${commonMod.mc}").joinToString("-")
// Stonecutter branches share the same Gradle project name (the MC version).
// Give internal components distinct module identities to prevent Gradle 9 from
// substituting a common project dependency with the current loader project.
group = "${commonMod.group}.${loader ?: "common"}"
val artifactBase = commonMod.propOrNull("rcui.artifact_base")
    ?: commonMod.id.replace('_', '-')
val publishedLoaderName = loader ?: "common"
val publishedArchiveBaseName = artifactBase
base { archivesName = publishedArchiveBaseName }
tasks.withType<AbstractArchiveTask>().configureEach {
    archiveBaseName.set(publishedArchiveBaseName)
    archiveFileName.set(project.provider {
        val classifier = archiveClassifier.orNull
            ?.takeIf(String::isNotEmpty)
            ?.let { "-$it" }
            ?: ""
        "$publishedArchiveBaseName-${project.version}-$publishedLoaderName$classifier.${archiveExtension.get()}"
    })
}
gradle.projectsEvaluated {
    tasks.withType<AbstractArchiveTask>().configureEach {
        archiveBaseName.set(publishedArchiveBaseName)
        archiveFileName.set(project.provider {
            val classifier = archiveClassifier.orNull
                ?.takeIf(String::isNotEmpty)
                ?.let { "-$it" }
                ?: ""
            "$publishedArchiveBaseName-${project.version}-$publishedLoaderName$classifier.${archiveExtension.get()}"
        })
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(configuredJavaVersion)
    sourceCompatibility = JavaVersion.toVersion(configuredJavaVersion)
    targetCompatibility = JavaVersion.toVersion(configuredJavaVersion)
    withSourcesJar()
}

// Keep the Gradle/IDE JVM independent from the JVM used to launch Minecraft.
// Old Minecraft/loader combinations must run on their supported Java runtime,
// even when Gradle itself is running on a newer JDK.
val runJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(configuredJavaVersion))
}
tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(runJavaLauncher)
}

// Some loader plugins replace the executable of their Minecraft run tasks after
// javaLauncher is configured. Restrict the late fallback to those run tasks so
// ordinary JavaExec tasks keep Gradle's normal toolchain behavior.
gradle.projectsEvaluated {
    tasks.withType<JavaExec>().matching { it.name in setOf("runClient", "runServer") }.configureEach {
        setExecutable(runJavaLauncher.get().executablePath.asFile.absolutePath)
    }
}

tasks.register("printRunJava") {
    group = "ide"
    description = "Prints the Java executable selected for Minecraft client/server run tasks."
    doLast {
        tasks.withType<JavaExec>().matching { it.name in setOf("runClient", "runServer") }.forEach {
            println("${project.path}:${it.name} -> ${it.executable}")
        }
    }
}

repositories {
    mavenCentral()
    // Fabric Loom may upgrade LWJGL when the Gradle JVM is Java 19+.
    // Loom 1.15.x expects this exclusive repository to exist before its
    // after-evaluation Minecraft setup runs. Register it up front so Gradle 9
    // does not reject a late content mutation of the Mojang repository.
    if (loader == null || loader == "fabric") {
        exclusiveContent {
            forRepository {
                maven {
                    name = "MavenCentralLWJGL"
                    url = uri("https://repo1.maven.org/maven2")
                }
            }
            filter { includeGroup("org.lwjgl") }
        }
    }
    maven("https://jitpack.io") {
        name = "JitPack"
        content { includeGroupByRegex("com\\.github\\..+") }
    }
    // Keep platform repositories isolated. Fabric Loom creates remapped
    // coordinates such as remapped.net.fabricmc.*; an outage in an unrelated
    // loader repository must not break Fabric dependency resolution.
    if (loader == null || loader == "fabric") {
        maven("https://maven.fabricmc.net/") {
            name = "FabricMC"
            content {
                includeGroupByRegex("net\\.fabricmc(?:\\..*)?")
                includeGroupByRegex("remapped\\.net\\.fabricmc(?:\\..*)?")
            }
        }
    }
    if (loader == "neoforge") {
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
            content {
                includeGroupByRegex("net\\.neoforged(?:\\..*)?")
                includeGroupByRegex("remapped\\.net\\.neoforged(?:\\..*)?")
            }
        }
    }
    if (loader == "forge") {
        maven("https://maven.minecraftforge.net/") {
            name = "MinecraftForge"
            content {
                includeGroupByRegex("net\\.minecraftforge(?:\\..*)?")
                includeGroupByRegex("remapped\\.net\\.minecraftforge(?:\\..*)?")
            }
        }
    }
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    if (loader == null || loader == "fabric") {
        maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
        // Mod Menu's optional text-placeholder integration resolves from Nucleoid's Maven.
        maven("https://maven.nucleoid.xyz") { name = "Nucleoid" }
    }
}

val resourcePackFormat = commonMod.propOrNull("resource_pack_format") ?: "15"
val usesMinorResourcePackFormat = commonMod.mc.let { version ->
    val parts = version.split('.').map(String::toInt)
    parts[0] >= 26 || (parts[0] == 1 && parts[1] == 21 && parts[2] >= 9)
}
val resourcePackMetadata = if (usesMinorResourcePackFormat) {
    "\"min_format\": [$resourcePackFormat, 0],\n        \"max_format\": [$resourcePackFormat, 0]"
} else {
    "\"pack_format\": $resourcePackFormat"
}

tasks.processResources {
    val values = mapOf(
        "modId" to commonMod.id,
        "modName" to commonMod.name,
        "modVersion" to commonMod.version,
        "modGroup" to commonMod.group,
        "modAuthor" to commonMod.author,
        "modDescription" to commonMod.description,
        "modLicense" to commonMod.license,
        "modGitHub" to commonMod.github,
        "minecraftVersion" to commonMod.mc,
        "resourcePackMetadata" to resourcePackMetadata,
        "javaVersion" to configuredJavaVersion.toString(),
        "mixinCompatibilityLevel" to mixinCompatibilityLevel,
        "minecraftVersionRangeFabric" to (commonMod.propOrNull("minecraft_version_range_fabric") ?: ""),
        "minecraftVersionRangeForge" to (commonMod.propOrNull("minecraft_version_range_forge") ?: ""),
        "minecraftVersionRangeNeoForge" to (commonMod.propOrNull("minecraft_version_range_neoforge") ?: ""),
        "fabricLoaderMinVersion" to (commonMod.depOrNull("fabric-loader-min") ?: ""),
        "forgeMinVersion" to (commonMod.depOrNull("forge-min") ?: ""),
        "neoForgeMinVersion" to (commonMod.depOrNull("neoforge-min") ?: "")
    )
    val jsonValues = values.mapValues { (_, value) -> value.replace("\n", "\\n") }
    filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) { expand(values) }
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(jsonValues) }
    filesMatching("pack.mcmeta") { expand(values) }
    inputs.properties(values)
}
tasks.named("processResources") { dependsOn(stonecutterGenerateTask) }

publishing {
    publications.register<MavenPublication>("mavenJava") {
        groupId = commonMod.group
        artifactId = "$artifactBase-mc${commonMod.mc}-$publishedLoaderName"
        version = commonMod.version
        from(components["java"])
    }
}
