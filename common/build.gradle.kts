import org.gradle.api.attributes.Attribute

plugins {
    id("multiloader-common")
    id("fabric-loom-compat")
}

dependencies {
    compileOnly(project(":core"))
}

configureMixinSupport(MixinTarget.COMMON)

val accessWidener = rootProject.file(
    "common/src/main/resources/accesswideners/${commonMod.mc}-${commonMod.id}.accesswidener"
)
check(accessWidener.isFile) { "Missing Access Widener: ${accessWidener.path}" }

loom {
    accessWidenerPath.set(accessWidener)
}

stonecutter {
    // Minecraft switched the vanilla logging API from Log4j2 to SLF4J in 1.18.2.
    // Keep shared sources on the modern API and rewrite them for older nodes.
    replacements.string(stonecutter.eval(commonMod.mc, "<=1.17.1")) {
        replace("import org.slf4j.LoggerFactory;", "import org.apache.logging.log4j.LogManager;")
    }
    replacements.string(stonecutter.eval(commonMod.mc, "<=1.17.1")) {
        replace("import org.slf4j.Logger;", "import org.apache.logging.log4j.Logger;")
    }
    replacements.string(stonecutter.eval(commonMod.mc, "<=1.17.1")) {
        replace("LoggerFactory.getLogger", "LogManager.getLogger")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    // Shared sources use SLF4J on modern nodes. Older nodes preprocess these
    // imports to Log4j, but the raw source set is still compiled by Gradle
    // before Stonecutter's generated source is consumed by loader projects.
    compileOnly("org.slf4j:slf4j-api:2.0.17")
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
}

val commonSourceKind = Attribute.of("com.rethinkqaq.configui.source-kind", String::class.java)
val commonJava = configurations.consumable("commonJava") {
    attributes.attribute(commonSourceKind, "java")
}
val commonResources = configurations.consumable("commonResources") {
    attributes.attribute(commonSourceKind, "resources")
}

val generatedMain = layout.buildDirectory.dir("generated/stonecutter/main")
artifacts {
    add(commonJava.name, generatedMain.map { it.dir("java") }) {
        builtBy(tasks.named("stonecutterGenerate"))
    }
    add(commonResources.name, generatedMain.map { it.dir("resources") }) {
        builtBy(tasks.named("stonecutterGenerate"))
    }
}

// `common` is an implementation detail that is merged into each platform jar.
// Do not leak a separately consumable artifact into the local Maven repository.
tasks.matching { it.name.startsWith("publish") }.configureEach {
    enabled = false
}
