import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.withGroovyBuilder

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.143"
}

apply(plugin = "dev.kikugie.fletching-table.neoforge")

val accessWidener = rootProject.file(
    "common/src/main/resources/accesswideners/${commonMod.mc}-${commonMod.id}.accesswidener"
)
check(accessWidener.isFile) { "Missing Access Widener: ${accessWidener.path}" }

extensions.getByName("fletchingTable").withGroovyBuilder {
    "accessConverter" {
        "register"("main") {
            "add"(accessWidener.absolutePath)
        }
    }
}

val snakeYaml = "org.snakeyaml:snakeyaml-engine:${providers.gradleProperty("rcui.snakeyaml_engine_version").get()}"
val snakeYamlBundle = configurations.detachedConfiguration(project.dependencies.create(snakeYaml))

dependencies {
    implementation(project(":core"))
    implementation(project(":config")) {
        (this as ModuleDependency).exclude(mapOf("group" to "org.snakeyaml", "module" to "snakeyaml-engine"))
    }
    compileOnly(snakeYaml)
    runtimeOnly(files({ snakeYamlBundle.files }))
}

// ModDev loads the registered source set directly during development rather than
// the finished jar. Make core part of that output as well as the final artifact.
val coreClasses = project(":core").layout.buildDirectory.dir("classes/java/main")
val configClasses = project(":config").layout.buildDirectory.dir("classes/java/main")
val configResources = project(":config").layout.buildDirectory.dir("resources/main")
sourceSets.named("main") {
    output.dir(coreClasses)
    output.dir(configClasses)
    output.dir(configResources)
}
tasks.named("classes") {
    dependsOn(":core:classes", ":config:classes", ":config:processResources")
}
tasks.named<Jar>("sourcesJar") {
    from(project(":core").file("src/main/java"))
    from(project(":config").file("src/main/java"))
}

tasks.named<Jar>("jar") {
    from(configClasses)
    from(configResources)
    from({ snakeYamlBundle.files.map { zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-neoforge"
        version = "${commonMod.mc}-${commonMod.version}"
    }
}

neoForge {
    version = commonMod.dep("neoforge")
    accessTransformers.from(project.file("build/resources/main/META-INF/accesstransformer.cfg"))
    if (!commonMod.unobfuscated && !providers.gradleProperty("skipOptionalDependencies").isPresent) {
        commonMod.depOrNull("parchment")?.let { parchmentVersion ->
            parchment {
                minecraftVersion = commonMod.mc
                mappingsVersion = parchmentVersion
            }
        }
    }
    runs {
        if (providers.gradleProperty("run.client").map { it.toBoolean() }.orElse(true).get()) {
            register("client") {
                client()
                ideName = "NeoForge Client ${commonMod.mc}"
                disableIdeRun()
                jvmArgument("-Drethink_config_ui_lib_example=true")
            }
        }
        if (providers.gradleProperty("run.server").map { it.toBoolean() }.orElse(true).get()) {
            register("server") {
                server()
                ideName = "NeoForge Server ${commonMod.mc}"
                disableIdeRun()
            }
        }
    }
    mods {
        register(commonMod.id) { sourceSet(sourceSets.main.get()) }
    }
}

tasks.named<ProcessResources>("processResources") {
    // Fletching Table omits an output file when an AW contains only its
    // header. NeoForm still expects the configured AT path to exist.
    doLast {
        val atFile = destinationDir.resolve("META-INF/accesstransformer.cfg")
        if (!atFile.exists()) {
            atFile.parentFile.mkdirs()
            atFile.writeText("# Generated from the versioned access widener.\n")
        }
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("processResources"))
}
