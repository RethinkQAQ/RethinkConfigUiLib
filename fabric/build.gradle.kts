import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("multiloader-loader")
    id("fabric-loom-compat")
}

dependencies {
    implementation(project(":core"))
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (!commonMod.unobfuscated) {
        mappings(loom.layered {
            officialMojangMappings()
            if (!providers.gradleProperty("skipOptionalDependencies").isPresent) {
                commonMod.depOrNull("parchment")?.let { parchmentVersion ->
                    parchment("org.parchmentmc.data:parchment-${commonMod.mc}:$parchmentVersion@zip")
                }
            }
        })
    }
    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric-loader")}")
}

// Older Fabric API modules publish Fabric Loader transitively. Keep the
// explicitly selected loader, while preventing every other mod dependency
// from contributing a second copy of FabricLoader classes.
configurations.configureEach {
    withDependencies {
        filterIsInstance<ModuleDependency>()
            .filterNot { it.group == "net.fabricmc" && it.name == "fabric-loader" }
            .forEach { it.exclude(group = "net.fabricmc", module = "fabric-loader") }
    }
}

tasks.jar {
    dependsOn(":core:classes")
    dependsOn(":common:${commonMod.mc}:classes")
    from(project(":core").layout.buildDirectory.dir("classes/java/main"))
    from(rootProject.project(":common:${commonMod.mc}").layout.buildDirectory.dir("classes/java/main"))
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        artifactId = "${providers.gradleProperty("rcui.artifact_base").get()}-fabric"
        version = "${commonMod.mc}-${commonMod.version}"
    }
}

loom {
    runs {
        if (providers.gradleProperty("run.client").map { it.toBoolean() }.orElse(true).get()) {
            named("client") {
                client()
                configName = "Fabric Client ${commonMod.mc}"
                // The template provides version-aware Gradle launchers from
                // generateIdeGradleRunConfigurations. Avoid IDEA Application
                // launchers, which bypass Gradle's selected Java toolchain.
                ideConfigGenerated(false)
                runDir("runs/client")
            }
        }
        if (providers.gradleProperty("run.server").map { it.toBoolean() }.orElse(true).get()) {
            named("server") {
                server()
                configName = "Fabric Server ${commonMod.mc}"
                ideConfigGenerated(false)
                runDir("runs/server")
            }
        }
    }
}
