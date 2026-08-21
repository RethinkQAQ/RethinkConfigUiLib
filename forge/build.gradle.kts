import net.minecraftforge.jarjar.gradle.JarJar
import net.minecraftforge.renamer.gradle.RenamerExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.kotlin.dsl.withGroovyBuilder

buildscript {
    repositories { maven("https://maven.minecraftforge.net/") }
    dependencies { classpath("net.minecraftforge:renamer-gradle:1.1.2") }
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

val runClient = providers.gradleProperty("run.client").map { it.toBoolean() }.orElse(true).get()
val runServer = providers.gradleProperty("run.server").map { it.toBoolean() }.orElse(true).get()

extensions.getByName("minecraft").withGroovyBuilder {
    "runs" {
        fun registerRun(name: String) {
            "create"(name) {
                "systemProperties"(mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug"
                ))
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

dependencies {
    implementation(project(":core"))
    implementation(minecraft.dependency("net.minecraftforge:forge:${commonMod.mc}-${commonMod.dep("forge")}"))
}

jarJar.register {
    archiveClassifier = if (legacyObfuscation) "slim" else null
}
val jarJarTask = tasks.named<JarJar>("jarJar")

// Registering the JarJar container creates the dedicated dependency configuration.
tasks.jar {
    dependsOn(":core:classes")
    dependsOn(":common:${commonMod.mc}:classes")
    from(project(":core").layout.buildDirectory.dir("classes/java/main"))
    from(rootProject.project(":common:${commonMod.mc}").layout.buildDirectory.dir("classes/java/main"))
}

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

// The Maven publication intentionally replaces the Java component's slim jar
// with the production JarJar/Renamer output. Gradle module metadata cannot model
// that artifact replacement, while the generated Maven POM can.
tasks.withType<GenerateModuleMetadata>().configureEach { enabled = false }
