import net.minecraftforge.jarjar.gradle.JarJar
import net.minecraftforge.renamer.gradle.RenamerExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar
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
    id("multiloader-loader")
    id("net.minecraftforge.gradle") version "[7.0.29,8.0)"
    id("net.minecraftforge.jarjar") version "0.2.3"
}

val legacyObfuscation = stonecutter.eval(commonMod.mc, "<=1.20.4")
if (legacyObfuscation) pluginManager.apply("net.minecraftforge.renamer")

minecraft {
    mappings("official", commonMod.mc)
}

// Temporary ForgeGradle 7.0.35 compatibility patch. Its
// SlimeLauncherMetadata task derives the @OutputFile `runsJson` from its own
// @OutputDirectory, which Gradle 9 refuses to query during task validation.
// Configure the same resolved archive path directly until the upstream fix ships.
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
                    // Keep local runs readable; registry dumps are only useful
                    // when explicitly diagnosing Forge data loading.
                    "forge.logging.console.level" to "info",
                    // ForgeGradle 7 applies this to its Slime Launcher run
                    // options when `runClient` executes. Released jars do not
                    // carry this development-only demo flag.
                    "rethink_config_ui_lib_example" to "true"
                )
                "systemProperties"(systemProperties)
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

val jarJarContainer = jarJar.register {
    archiveClassifier = if (legacyObfuscation) "slim" else null
}
dependencies {
    implementation(project(":core"))
    val configDependency = project.dependencies.project(mapOf("path" to ":config"))
    // Config classes are merged into the Forge development output below. Keep
    // the project dependency off Forge's runtime module path to avoid a JPMS
    // split package between the standalone config module and main.
    compileOnly(configDependency)
    val snakeYaml = "org.snakeyaml:snakeyaml-engine:${providers.gradleProperty("rcui.snakeyaml_engine_version").get()}"
    implementation(snakeYaml)
    add(jarJarContainer.configurationName, snakeYaml)
    implementation(minecraft.dependency("net.minecraftforge:forge:${commonMod.mc}-${commonMod.dep("forge")}"))
}
val jarJarTask = tasks.named<JarJar>("jarJar")

if (legacyObfuscation) {
    val renamerExtension = extensions.getByType<RenamerExtension>()
    // Forge's legacy renamer requires a Mixin refmap provider even when this
    // library declares no runtime Mixin configuration.
    renamerExtension.enableMixinRefmaps {
        config("${commonMod.id}.forge.build-only.mixins.json")
    }
    renamerExtension.mappings(minecraft.dependency.toSrg)
    val productionJar = renamerExtension.classes("renameJar", jarJarTask) {
        archiveClassifier.set("all")
        mappings(renamerExtension.mixin.generatedMappings)
    }
    tasks.named("build") { dependsOn(productionJar) }
    afterEvaluate {
        publishing.publications.named<MavenPublication>("mavenJava") {
            artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-forge"
            version = "${commonMod.mc}-${commonMod.version}"
            artifacts.clear()
            artifact(productionJar) { classifier = null }
            artifact(tasks.named("sourcesJar"))
        }
    }
} else {
    afterEvaluate {
        publishing.publications.named<MavenPublication>("mavenJava") {
            artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-forge"
            version = "${commonMod.mc}-${commonMod.version}"
            artifacts.clear()
            artifact(jarJarTask) { classifier = null }
            artifact(tasks.named("sourcesJar"))
        }
    }
}

tasks.jar {
    archiveClassifier = "slim"
}

// ForgeGradle 7's userdev locator registers the resources directory as the
// development mod file, but does not scan the corresponding classes directory
// when the sources come from Stonecutter-generated trees. For the local Forge
// run only, put the resources beside the compiled classes. Keeping one output
// directory prevents Forge's module layer from seeing a split package ("main"
// plus the mod file).
val forgeCompileClasses = layout.buildDirectory.dir("classes/java/main")
val configClasses = project(":config").layout.buildDirectory.dir("classes/java/main")
sourceSets["main"].output.dir(configClasses)
tasks.named("classes") {
    dependsOn(":core:classes", ":config:classes")
}
tasks.named<Jar>("sourcesJar") {
    from(project(":core").file("src/main/java"))
    from(project(":config").file("src/main/java"))
}
sourceSets["main"].output.setResourcesDir(forgeCompileClasses)
val prepareForgeDevMod = tasks.register("prepareForgeDevMod") {
    dependsOn("classes", "processResources")
}
tasks.matching { it.name in setOf("runClient", "runServer") }.configureEach {
    dependsOn(prepareForgeDevMod)
}

// The Maven publication intentionally replaces the Java component's slim jar
// with the production JarJar/Renamer output. Gradle module metadata cannot model
// that artifact replacement, while the generated Maven POM can.
tasks.withType<GenerateModuleMetadata>().configureEach { enabled = false }
