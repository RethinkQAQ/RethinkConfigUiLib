import java.util.Properties
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar

private fun String.xmlEscape() = buildString {
    this@xmlEscape.forEach { character ->
        append(
            when (character) {
                '&' -> "&amp;"
                '"' -> "&quot;"
                '\'' -> "&apos;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                else -> character
            }
        )
    }
}

val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = !isCi
gradle.startParameter.isBuildCacheEnabled = true
gradle.startParameter.isConfigureOnDemand = !isCi

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val supportedVersions = providers.gradleProperty("stonecutter_enabled_versions")
    .get().split(',').map(String::trim).filter(String::isNotEmpty)
val allowedPlatforms = setOf("fabric", "forge", "neoforge")

check(supportedVersions.isNotEmpty()) { "stonecutter_enabled_versions must contain at least one version" }
check(supportedVersions.size == supportedVersions.toSet().size) {
    "stonecutter_enabled_versions contains duplicate versions"
}
val configuredVcsVersion = providers.gradleProperty("stonecutter_vcs_version")
    .orNull
    ?.trim()
    ?.takeIf(String::isNotEmpty)
val activeVcsVersion = configuredVcsVersion ?: supportedVersions.first()
check(activeVcsVersion in supportedVersions) {
    "stonecutter_vcs_version=$activeVcsVersion must also be listed in stonecutter_enabled_versions"
}
check(supportedVersions.all { version ->
    val core = version.substringBefore('-')
    val parts = core.split('.')
    parts.size in 2..3 && parts.all { part -> part.isNotEmpty() && part.all(Char::isDigit) }
}) {
    "stonecutter_enabled_versions contains an invalid Minecraft version"
}

fun versionProperties(version: String): Properties = Properties().apply {
    val source = file("versions/$version/gradle.properties")
    check(source.isFile) { "Missing version properties file: ${source.path}" }
    source.inputStream().use(::load)
}

fun enabledPlatforms(version: String): Set<String> {
    val value = versionProperties(version).getProperty("enable_platforms")
        ?: error("Version $version is missing enable_platforms")
    val platforms = value.split(',').map(String::trim).filter(String::isNotEmpty).toSet()
    check(platforms.isNotEmpty()) { "Version $version enables no platforms" }
    check(platforms.all { it in allowedPlatforms }) {
        "Version $version contains unsupported platforms: ${platforms - allowedPlatforms}"
    }
    return platforms
}

val platformVersions = allowedPlatforms.associateWith { platform ->
    supportedVersions.filter { platform in enabledPlatforms(it) }
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(*supportedVersions.toTypedArray())
        vcsVersion = activeVcsVersion
        branch("common") { versions(*supportedVersions.toTypedArray()) }
        platformVersions.forEach { (platform, versions) ->
            if (versions.isNotEmpty()) {
                branch(platform) { versions(*versions.toTypedArray()) }
            }
        }
    }
}

rootProject.name = "rethink-config-ui-lib"
include(":core", ":config")

gradle.projectsLoaded {
    rootProject.pluginManager.apply("base")
    rootProject.group = providers.gradleProperty("mod.group").get()
    rootProject.version = providers.gradleProperty("mod.version").get()
    val validateVersionProperties = rootProject.tasks.register("validateVersionProperties") {
        group = "verification"
        inputs.files(supportedVersions.map { rootProject.layout.projectDirectory.file("versions/$it/gradle.properties") })
        doLast {
            supportedVersions.forEach { version ->
                val values = versionProperties(version)
                listOf("enable_platforms", "minecraft_version", "publish_version").forEach { key ->
                    check(!values.getProperty(key).isNullOrBlank()) {
                        "versions/$version/gradle.properties is missing $key"
                    }
                }
                check(values.getProperty("minecraft_version") == version) {
                    "versions/$version/gradle.properties must declare minecraft_version=$version"
                }
                enabledPlatforms(version).forEach { platform ->
                    check(!values.getProperty("minecraft_version_range_$platform").isNullOrBlank()) {
                        "versions/$version/gradle.properties is missing minecraft_version_range_$platform"
                    }
                }
            }
        }
    }
    val validateLicenseFile = rootProject.tasks.register("validateLicenseFile") {
        group = "verification"
        inputs.file(rootProject.layout.projectDirectory.file("LICENSE"))
        doLast {
            val license = rootProject.file("LICENSE")
            check(license.isFile) { "Missing root LICENSE file" }
            val text = license.readText()
            check(text.trimStart().startsWith("GNU LESSER GENERAL PUBLIC LICENSE")) {
                "LICENSE must contain the standard GNU LGPL v3 text"
            }
            check("Version 3, 29 June 2007" in text) {
                "LICENSE must contain the standard LGPL v3 version header"
            }
        }
    }
    val loaderTaskPaths = platformVersions.flatMap { (platform, versions) ->
        versions.map { version -> ":$platform:$version:build" }
    }
    val licenseTaskPaths = buildList {
        add(":core:licenseCheck")
        add(":config:licenseCheck")
        supportedVersions.forEach { version -> add(":common:$version:licenseCheck") }
        platformVersions.forEach { (platform, versions) ->
            versions.forEach { version -> add(":$platform:$version:licenseCheck") }
        }
    }

    rootProject.tasks.named("build") {
        dependsOn(":config:build")
        dependsOn(loaderTaskPaths)
        dependsOn(validateVersionProperties)
        dependsOn(validateLicenseFile)
    }
    rootProject.tasks.register("collectJars", Copy::class.java) {
        group = "build"
        dependsOn(rootProject.tasks.named("build"))
        into(rootProject.layout.buildDirectory.dir("mod-jars"))
        platformVersions.forEach { (platform, versions) ->
            versions.forEach { version ->
                val candidate = rootProject.project(":$platform:$version")
                from(candidate.layout.buildDirectory.dir("libs")) {
                    include("*.jar")
                    exclude("*-sources.jar", "*-javadoc.jar", "*-dev.jar", "*-slim.jar")
                    // Forge's legacy remapper emits the final production jar
                    // as -slim-all; publish it with the normal mod filename.
                    rename { name ->
                        if (name.endsWith("-slim-all.jar")) {
                            name.removeSuffix("-slim-all.jar") + ".jar"
                        } else {
                            name
                        }
                    }
                }
            }
        }
    }
    val artifactBase = providers.gradleProperty("rcui.artifact_base")
        .orElse(providers.gradleProperty("mod.id").map { it.replace('_', '-') })
        .get()
    val buildVersion = listOfNotNull(
        providers.gradleProperty("mod.version").get(),
        providers.gradleProperty("build.number").orNull?.trim()?.takeIf(String::isNotEmpty)
    ).joinToString("-")
    val universalJarTasks = platformVersions.values.flatten().distinct().map { minecraftVersion ->
        val platforms = listOf("fabric", "forge", "neoforge")
            .filter { platform -> platformVersions[platform]?.contains(minecraftVersion) == true }
        val taskName = "universalJar${minecraftVersion.replace('.', '_')}"
        rootProject.tasks.register(taskName, Jar::class.java) {
            group = "build"
            description = "Builds one experimental universal RCUI JAR for Minecraft $minecraftVersion."
            dependsOn(platforms.map { platform -> ":$platform:$minecraftVersion:build" })
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            archiveFileName.set("$artifactBase-$buildVersion-mc$minecraftVersion.jar")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            platforms.forEach { platform ->
                val candidate = rootProject.project(":$platform:$minecraftVersion")
                val sourceName = if (platform == "forge" && minecraftVersion in setOf("1.20.1", "1.20.4")) {
                    "$artifactBase-$buildVersion-mc$minecraftVersion-$platform-slim-all.jar"
                } else {
                    "$artifactBase-$buildVersion-mc$minecraftVersion-$platform.jar"
                }
                from(rootProject.zipTree(candidate.layout.buildDirectory.file("libs/$sourceName")))
            }
        }
    }
    rootProject.tasks.register("assembleUniversalJars") {
        group = "build"
        description = "Builds one experimental universal JAR for each supported Minecraft version."
        dependsOn(universalJarTasks)
    }
    rootProject.tasks.register("licenseFormat") {
        group = "formatting"
        dependsOn(licenseTaskPaths.map { it.replace(":licenseCheck", ":licenseFormat") })
    }
    rootProject.tasks.register("licenseCheck") {
        group = "verification"
        dependsOn(licenseTaskPaths)
        dependsOn(validateLicenseFile)
    }

    val runClient = providers.gradleProperty("run.client").map { it.toBoolean() }.orElse(true)
    val runServer = providers.gradleProperty("run.server").map { it.toBoolean() }.orElse(true)
    val ideRunConfigurations = rootProject.layout.projectDirectory.dir(".idea/runConfigurations")
    val generateIdeGradleRunConfigurations = rootProject.tasks.register("generateIdeGradleRunConfigurations") {
        group = "ide"
        description = "Generates IntelliJ Gradle run configurations for registered loaders and Minecraft versions."
        inputs.property("run.client", runClient)
        inputs.property("run.server", runServer)
        inputs.property("platformVersions", platformVersions.mapValues { it.value.joinToString(",") })
        outputs.dir(ideRunConfigurations)

        doLast {
            val directory = ideRunConfigurations.asFile
            check(directory.exists() || directory.mkdirs()) {
                "Could not create IntelliJ run configuration directory: ${directory.path}"
            }
            directory.listFiles { file ->
                file.isFile && file.name.startsWith("Stonecutter_Gradle_") && file.extension == "xml"
            }?.forEach { file ->
                check(file.delete()) { "Could not remove stale generated run configuration: ${file.path}" }
            }

            fun writeRunConfiguration(platform: String, minecraftVersion: String, side: String) {
                val displayPlatform = when (platform) {
                    "neoforge" -> "NeoForge"
                    else -> platform.replaceFirstChar(Char::uppercase)
                }
                val displaySide = side.replaceFirstChar(Char::uppercase)
                val taskName = ":$platform:$minecraftVersion:run$displaySide"
                val configurationName = "$displayPlatform $displaySide $minecraftVersion (Gradle)"
                val fileName = "Stonecutter_Gradle_${platform}_${minecraftVersion.replace('.', '_')}_${side}.xml"
                directory.resolve(fileName).writeText(
                    """
                    |<component name="ProjectRunConfigurationManager">
                    |  <configuration default="false" factoryName="Gradle" name="${configurationName.xmlEscape()}" type="GradleRunConfiguration">
                    |    <ExternalSystemSettings>
                    |      <option name="executionName" />
                    |      <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                    |      <option name="externalSystemIdString" value="GRADLE" />
                    |      <option name="scriptParameters" />
                    |      <option name="taskDescriptions"><list /></option>
                    |      <option name="taskNames"><list><option value="${taskName.xmlEscape()}" /></list></option>
                    |      <option name="vmOptions" />
                    |    </ExternalSystemSettings>
                    |    <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                    |    <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                    |    <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
                    |    <DebugAllEnabled>false</DebugAllEnabled>
                    |    <RunAsTest>false</RunAsTest>
                    |    <GradleProfilingDisabled>false</GradleProfilingDisabled>
                    |    <GradleCoverageDisabled>false</GradleCoverageDisabled>
                    |    <method v="2" />
                    |  </configuration>
                    |</component>
                    |""".trimMargin() + System.lineSeparator()
                )
            }

            platformVersions.forEach { (platform, versions) ->
                versions.forEach { minecraftVersion ->
                    if (runClient.get()) writeRunConfiguration(platform, minecraftVersion, "client")
                    if (runServer.get()) writeRunConfiguration(platform, minecraftVersion, "server")
                }
            }
        }
    }
    rootProject.tasks.matching { it.name == "stonecutterIdea" }.configureEach {
        dependsOn(generateIdeGradleRunConfigurations)
    }
}
