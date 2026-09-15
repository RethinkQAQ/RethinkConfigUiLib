import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.file.Directory
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven

class MinecraftVersionInfo(val raw: String) {
    private val parts = raw.substringBefore('-')
        .split('.')
        .map { it.toIntOrNull() ?: 0 }

    val major: Int get() = parts.getOrElse(0) { 0 }
    val minor: Int get() = parts.getOrElse(1) { 0 }
    val patch: Int get() = parts.getOrElse(2) { 0 }
    val isUnobfuscated: Boolean get() = major >= 26

    fun atLeast(major: Int, minor: Int, patch: Int = 0): Boolean =
        this.major > major ||
            (this.major == major && this.minor > minor) ||
            (this.major == major && this.minor == minor && this.patch >= patch)
}

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

val Project.stonecutterBuild get() = extensions.getByType<StonecutterBuildExtension>()
val Project.commonProject get() = rootProject.project(stonecutterBuild.current.project)
val Project.commonMod get() = commonProject.mod
val Project.loader: String? get() = prop("loader")
val Project.stonecutterGeneratedMain: Provider<Directory>
    get() = stonecutterBuild.tasks.generatedSourcesDir.map { it.dir("main") }
@Suppress("OPT_IN_USAGE")
val Project.stonecutterGenerateTask: org.gradle.api.tasks.TaskProvider<Sync>
    get() = requireNotNull(stonecutterBuild.tasks.generate["main"]) {
        "Stonecutter did not register a generate task for the main source set in $project"
    }

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = modProp("id")
    val name: String get() = modProp("name")
    val version: String get() = modProp("version")
    val group: String get() = modProp("group")
    val author: String get() = modProp("author")
    val description: String get() = modProp("description")
    val license: String get() = modProp("license")
    val github: String get() = modProp("github")
    val mc: String get() = prop("minecraft_version")
    val minecraftVersion: MinecraftVersionInfo get() = MinecraftVersionInfo(mc)
    val unobfuscated: Boolean get() = minecraftVersion.isUnobfuscated

    fun propOrNull(key: String) = project.prop(key)?.takeIf(String::isNotBlank)
    fun prop(key: String) = requireNotNull(propOrNull(key)) { "Missing '$key'" }
    fun modProp(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key'" }
    fun depOrNull(key: String) = propOrNull("deps.$key")
    fun dep(key: String) = requireNotNull(depOrNull(key)) { "Missing 'deps.$key'" }
}
