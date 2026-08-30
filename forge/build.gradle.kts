import net.minecraftforge.jarjar.gradle.JarJar
import net.minecraftforge.jarjar.gradle.JarJarExtension
import net.minecraftforge.renamer.gradle.RenamerExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.withGroovyBuilder

buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net/")
    }
    dependencies {
        classpath("net.minecraftforge:renamer-gradle:1.1.2")
        classpath("org.apache.maven:maven-artifact:3.9.9")
    }
}

plugins {
    id("net.minecraftforge.accesstransformers") version "2.0.0"
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("multiloader-loader")
}

apply(plugin = "dev.kikugie.fletching-table")

val forgeVersionParts = commonMod.mc.split('.').mapNotNull(String::toIntOrNull)
val forgeMajor = forgeVersionParts.getOrElse(0) { 0 }
val forgeMinor = forgeVersionParts.getOrElse(1) { 0 }
val forgePatch = forgeVersionParts.getOrElse(2) { 0 }
val legacyObfuscation = forgeMajor == 1 && (forgeMinor < 20 || forgeMinor == 20 && forgePatch <= 4)
val supportsJarJar = forgeMajor >= 26 || forgeMajor > 1 || forgeMinor >= 17
if (supportsJarJar) pluginManager.apply("net.minecraftforge.jarjar")

val accessWidener = rootProject.file(
    "common/src/main/resources/accesswideners/${commonMod.mc}-${commonMod.id}.accesswidener"
)
check(accessWidener.isFile) { "Missing Access Widener: ${accessWidener.path}" }

minecraft {
    mappings("official", commonMod.mc)
    useDefaultAccessTransformer()
}


tasks.matching { it.name == "slimeLauncherMetadataForForge" }.configureEach {
    var type: Class<*>? = javaClass
    var runsJsonGetter: java.lang.reflect.Method? = null
    while (type != null && runsJsonGetter == null) {
        runsJsonGetter = runCatching { type.getDeclaredMethod("getRunsJson") }.getOrNull()
        type = type.superclass
    }
    val getter = runsJsonGetter
        ?: error("ForgeGradle metadata task no longer exposes runsJson")
    getter.isAccessible = true
    val runsJson = getter.invoke(this) as RegularFileProperty
    runsJson.set(layout.buildDirectory.file("minecraftforge/forgegradle/slimeLauncherMetadataForForge/launcher/runs.json"))
}

val runClient = providers.gradleProperty("run.client").map { it.toBoolean() }.orElse(true).get()
val runServer = providers.gradleProperty("run.server").map { it.toBoolean() }.orElse(true).get()

extensions.getByName("minecraft").withGroovyBuilder {
    "runs" {
        fun registerRun(name: String) {
            "create"(name) {
                val systemProperties = mutableMapOf(
                    "forge.logging.console.level" to "info",
                    "rethink_config_ui_lib_example" to "true"
                )
                "systemProperties"(systemProperties)
                "args"("-mixin.config=${commonMod.id}.mixins.json")
                "args"("-mixin.config=${commonMod.id}.forge.mixins.json")
                if (commonMod.mc.split('.').getOrNull(1)?.toIntOrNull()?.let { it >= 17 } == true) {
                    "jvmArgs"("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
                }
                setProperty("workingDir", project.file("run"))
                setProperty("client", name == "client")
                "mods" {
                    "create"(commonMod.id) {
                        "source"(sourceSets["main"])
                    }
                }
            }
        }
        if (runClient) registerRun("client")
        if (runServer) registerRun("server")
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

val minecraftExtension = extensions.getByName("minecraft")
val forgeDependency = requireNotNull(minecraftExtension.withGroovyBuilder {
    "dependency"("net.minecraftforge:forge:${commonMod.mc}-${commonMod.dep("forge")}")
})

val snakeYaml = "org.snakeyaml:snakeyaml-engine:${providers.gradleProperty("rcui.snakeyaml_engine_version").get()}"
val snakeYamlBundle = configurations.detachedConfiguration(project.dependencies.create(snakeYaml))
val configClasses = project(":config").layout.buildDirectory.dir("classes/java/main")
val configResources = project(":config").layout.buildDirectory.dir("resources/main")

dependencies {
    implementation(project(":core"))
    implementation(forgeDependency)
    val configDependency = project.dependencies.project(mapOf("path" to ":config"))
    compileOnly(configDependency)
    compileOnly(snakeYaml)
    runtimeOnly(files({ snakeYamlBundle.files }))
}

configureMixinSupport(MixinTarget.FORGE)
mixin {
    add(sourceSets["main"], "${commonMod.id}.refmap.json")
    config("${commonMod.id}.mixins.json")
    config("${commonMod.id}.forge.mixins.json")
}

extensions.getByName("fletchingTable").withGroovyBuilder {
    "accessConverter" {
        "register"("main") {
            "add"(accessWidener.absolutePath)
        }
    }
}

if (supportsJarJar) {
    tasks.withType<JarJar>().configureEach {
        from(configClasses)
        from(configResources)
        from({ snakeYamlBundle.files.map { zipTree(it) } }) {
            exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
        }
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    }
} else {
    tasks.jar {
        from(configClasses)
        from(configResources)
        from({ snakeYamlBundle.files.map { zipTree(it) } }) {
            exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
        }
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    }
}

val jarJarTask = if (supportsJarJar) {
    extensions.getByType<JarJarExtension>().register()
    tasks.named<JarJar>("jarJar") { archiveClassifier.set("all") }
} else null

if (legacyObfuscation && jarJarTask != null) {
    pluginManager.apply("net.minecraftforge.renamer")
    val renamer = extensions.getByType<RenamerExtension>()
    renamer.enableMixinRefmaps {
        config("${commonMod.id}.mixins.json")
        config("${commonMod.id}.forge.mixins.json")
    }
    val toSrg = requireNotNull(forgeDependency.withGroovyBuilder { getProperty("toSrg") })
    renamer.mappings(toSrg as Provider<*>)
    val productionJar = renamer.classes("renameJar", jarJarTask) {
        archiveClassifier.set("all")
        mappings(renamer.mixin.generatedMappings)
    }
    tasks.named("build") { dependsOn(productionJar) }
    afterEvaluate {
        publishing.publications.named<MavenPublication>("mavenJava") {
            artifacts.clear()
            artifact(productionJar) { classifier = null }
            artifact(tasks.named("sourcesJar"))
        }
    }
} else if (jarJarTask != null) {
    afterEvaluate {
        publishing.publications.named<MavenPublication>("mavenJava") {
            artifacts.clear()
            artifact(jarJarTask) { classifier = null }
            artifact(tasks.named("sourcesJar"))
        }
    }
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-forge"
        version = "${commonMod.mc}-${commonMod.version}"
    }
}

tasks.jar {
    archiveClassifier = "slim"
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
}

tasks.named("classes") {
    dependsOn(":core:classes", ":config:classes", ":config:processResources")
}
tasks.named<Jar>("sourcesJar") {
    from(project(":core").file("src/main/java"))
    from(project(":config").file("src/main/java"))
}
sourceSets.named("main") {
    val outputDirectory = layout.buildDirectory.dir("sourceSets/$name")
    java.destinationDirectory.set(outputDirectory)
    output.setResourcesDir(outputDirectory.get().asFile)
    resources.srcDir(layout.buildDirectory.dir("generated/access-transformer"))
}
tasks.named<ProcessResources>("processResources") {
    dependsOn(":config:processResources")
    doLast {
        // Header-only AW files are valid and Fletching Table intentionally
        // omits an empty AT. Forge still expects the standard resource when
        // the access transformer integration is enabled.
        val atFile = destinationDir.resolve("META-INF/accesstransformer.cfg")
        if (!atFile.exists()) {
            atFile.parentFile.mkdirs()
            atFile.writeText("# Generated from the versioned access widener.\n")
        }
    }
}
val prepareForgeDevMod = tasks.register("prepareForgeDevMod") {
    dependsOn("classes", "processResources")
}
tasks.matching { it.name in setOf("runClient", "runServer") }.configureEach {
    dependsOn(prepareForgeDevMod)
}

tasks.withType<GenerateModuleMetadata>().configureEach { enabled = false }
