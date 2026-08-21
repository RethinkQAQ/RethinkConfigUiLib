import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.143"
}

dependencies {
    implementation(project(":core"))
}

// ModDev loads the registered source set directly during development rather than
// the finished jar. Make core part of that output as well as the final artifact.
val coreClasses = project(":core").layout.buildDirectory.dir("classes/java/main")
sourceSets.named("main") {
    output.dir(coreClasses)
}
tasks.named("classes") {
    dependsOn(":core:classes")
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
