import org.gradle.api.attributes.Attribute

plugins {
    java
    idea
    id("multiloader-common")
}

val commonPath = ":common:${commonMod.mc}"
val commonSourceKind = Attribute.of("com.rethinkqaq.configui.source-kind", String::class.java)
val commonJavaDependencies = configurations.dependencyScope("commonJavaDependencies")
val commonResourcesDependencies = configurations.dependencyScope("commonResourcesDependencies")
val commonJava = configurations.resolvable("commonJava") {
    extendsFrom(commonJavaDependencies.get())
    attributes.attribute(commonSourceKind, "java")
}
val commonResources = configurations.resolvable("commonResources") {
    extendsFrom(commonResourcesDependencies.get())
    attributes.attribute(commonSourceKind, "resources")
}

dependencies {
    compileOnly(project(commonPath))
    // The common adapter is compiled into this loader's output by compileJava.
    // Do not add the common project jar to runtime: Forge 1.21.1 treats that
    // jar as a named module, creating a split package with main at launch.
    val commonJavaProject = project.dependencies.project(commonPath)
    val commonResourcesProject = project.dependencies.project(commonPath)
    add(commonJavaDependencies.name, commonJavaProject)
    add(commonResourcesDependencies.name, commonResourcesProject)
}

val generatedPlatformJava = layout.buildDirectory.dir("generated/stonecutter/main/java")
tasks.compileJava {
    // Compile the Stonecutter-generated platform sources instead of the raw
    // branch sources. This is required for platform-specific preprocessing
    // such as ResourceLocation/Identifier and legacy mappings.
    dependsOn("stonecutterGenerate")
    setSource(files(generatedPlatformJava, commonJava))
}
tasks.processResources {
    from(commonResources) {
        exclude("accesswideners/**")
    }
}
