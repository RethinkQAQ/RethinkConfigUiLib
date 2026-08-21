import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.time.Year

plugins {
    java
    idea
    `java-library`
    `maven-publish`
    id("com.github.hierynomus.license")
}

val stonecutterGenerateTask = ":common:${commonMod.prop("minecraft_version")}:stonecutterGenerate"
val licenseFormat = tasks.named("licenseFormat") {
    dependsOn(stonecutterGenerateTask)
}
tasks.register("licenseCheck") {
    group = "verification"
    dependsOn("licenseMain")
}
tasks.withType<JavaCompile>().configureEach { dependsOn(licenseFormat) }

extensions.getByName("license").withGroovyBuilder {
    setProperty("header", rootProject.file("HEADER.txt"))
    setProperty("skipExistingHeaders", true)
    setProperty("ignoreFailures", false)
    "include"("**/*.java")
    "include"("**/*.kt")
    "include"("**/*.kts")
    "include"("**/*.groovy")
    "include"("**/*.gradle")
    "ext" {
        setProperty("name", commonMod.name)
        setProperty("author", commonMod.author)
        setProperty("year", Year.now().value.toString())
    }
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
base { archivesName = "${commonMod.id}-${loader ?: "common"}" }

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
    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) { expand(jsonValues) }
    inputs.properties(values)
}
tasks.named("processResources") { dependsOn(stonecutterGenerateTask) }

publishing {
    publications.register<MavenPublication>("mavenJava") {
        groupId = commonMod.group
        artifactId = "${commonMod.id}-${loader ?: "common"}-mc${commonMod.mc}"
        version = commonMod.version
        from(components["java"])
    }
}
