import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.143"
}

dependencies {
    implementation(project(":core"))
}

tasks.jar {
    dependsOn(":core:classes")
    dependsOn(":common:${commonMod.mc}:classes")
    from(project(":core").layout.buildDirectory.dir("classes/java/main"))
    from(rootProject.project(":common:${commonMod.mc}").layout.buildDirectory.dir("classes/java/main"))
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
