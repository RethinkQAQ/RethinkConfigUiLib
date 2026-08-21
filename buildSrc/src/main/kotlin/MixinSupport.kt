import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

private const val MIXIN_VERSION = "0.8.5"
private const val MIXIN_EXTRAS_VERSION = "0.5.4"

enum class MixinTarget { COMMON, FABRIC, FORGE, NEOFORGE }

fun Project.configureMixinSupport(target: MixinTarget) {
    if (target == MixinTarget.COMMON) {
        dependencies.add("compileOnly", "org.spongepowered:mixin:$MIXIN_VERSION")
        dependencies.add("compileOnly", "io.github.llamalad7:mixinextras-common:$MIXIN_EXTRAS_VERSION")
    }
    if (target != MixinTarget.NEOFORGE) {
        dependencies.add("annotationProcessor", "io.github.llamalad7:mixinextras-common:$MIXIN_EXTRAS_VERSION")
        if (!commonMod.unobfuscated) {
            dependencies.add("annotationProcessor", "org.spongepowered:mixin:$MIXIN_VERSION:processor")
        }
    }
    when (target) {
        MixinTarget.FABRIC -> {
            val dependency = requireNotNull(dependencies.add(
                "modImplementation", "io.github.llamalad7:mixinextras-fabric:$MIXIN_EXTRAS_VERSION"
            ))
            dependencies.add("include", dependency)
        }
        MixinTarget.FORGE -> {
            dependencies.add("implementation", "io.github.llamalad7:mixinextras-forge:$MIXIN_EXTRAS_VERSION")
            dependencies.add("jarJar", "io.github.llamalad7:mixinextras-forge:$MIXIN_EXTRAS_VERSION")
        }
        else -> Unit
    }
    if (target == MixinTarget.FORGE || target == MixinTarget.NEOFORGE || commonMod.unobfuscated) {
        configureEmptyRefmap()
    }
}

private fun Project.configureEmptyRefmap() {
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
    tasks.named<ProcessResources>("processResources") {
        dependsOn(generateMixinRefmap)
        from(generatedRefmap.map { it.asFile.parentFile })
    }
}
