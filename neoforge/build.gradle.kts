import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.143"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":config"))
    val snakeYaml = "org.snakeyaml:snakeyaml-engine:${providers.gradleProperty("rcui.snakeyaml_engine_version").get()}"
    implementation(snakeYaml)
    add("jarJar", snakeYaml)
}

// ModDev loads the registered source set directly during development rather than
// the finished jar. Make core part of that output as well as the final artifact.
val coreClasses = project(":core").layout.buildDirectory.dir("classes/java/main")
val configClasses = project(":config").layout.buildDirectory.dir("classes/java/main")
sourceSets.named("main") {
    output.dir(coreClasses)
    output.dir(configClasses)
}
tasks.named("classes") {
    dependsOn(":core:classes", ":config:classes")
}
tasks.named<Jar>("sourcesJar") {
    from(project(":core").file("src/main/java"))
    from(project(":config").file("src/main/java"))
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-neoforge"
        version = "${commonMod.mc}-${commonMod.version}"
    }
}

neoForge {
    version = commonMod.dep("neoforge")
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
