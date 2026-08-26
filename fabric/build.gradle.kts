import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    id("multiloader-loader")
    id("fabric-loom-compat")
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")
    if (!commonMod.unobfuscated) {
        annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    }
    implementation(project(":core"))
    val configDependency = project.dependencies.project(mapOf("path" to ":config"))
    implementation(configDependency)
    val snakeYaml = "org.snakeyaml:snakeyaml-engine:${providers.gradleProperty("rcui.snakeyaml_engine_version").get()}"
    implementation(snakeYaml)
    include(snakeYaml)
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
    modImplementation("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric-api")}")

    if (!providers.gradleProperty("skipOptionalDependencies").isPresent) {
        commonMod.depOrNull("modmenu")?.let { modLocalRuntime("com.terraformersmc:modmenu:$it") }
    }
}

if (!commonMod.unobfuscated) {
    loom {
        mixin {
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("${commonMod.id}.refmap.json")
        }
    }
} else {
    val generatedRefmap = layout.buildDirectory.file("generated/resources/mixin/${commonMod.id}.refmap.json")
    val generateMixinRefmap = tasks.register("generateMixinRefmap") {
        outputs.file(generatedRefmap)
        doLast {
            generatedRefmap.get().asFile.apply {
                parentFile.mkdirs()
                writeText("{\n  \"mappings\": {},\n  \"data\": {}\n}\n")
            }
        }
    }
    tasks.processResources {
        dependsOn(generateMixinRefmap)
        from(generatedRefmap.map { it.asFile.parentFile })
    }
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

// Loom development runs use the source-set output directly. Include core there
// too, while the normal Java jar task packages the same output for distribution.
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
                property("rethink_config_ui_lib_example", "true")
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
