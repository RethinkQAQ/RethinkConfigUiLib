import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withGroovyBuilder
import nl.javadude.gradle.plugins.license.header.HeaderDefinitionBuilder
import java.time.Year

plugins {
    id("com.github.hierynomus.license")
}

val licenseFormat = tasks.named("licenseFormat")
tasks.named("licenseMain") {
    dependsOn(licenseFormat)
}
tasks.register("licenseCheck") {
    group = "verification"
    dependsOn("licenseMain")
}
tasks.withType<JavaCompile>().configureEach {
    dependsOn(licenseFormat)
}

extensions.getByName("license").withGroovyBuilder {
    setProperty("header", rootProject.file("HEADER.txt"))
    // Keep valid headers in place after the one-time normalization, matching
    // the template's license check behavior.
    setProperty("skipExistingHeaders", true)
    setProperty("ignoreFailures", false)
    // Keep a normal /* ... */ header while making the closing delimiter's
    // trailing newline explicit for LicenseCheck consistency.
    "headerDefinition"(
        HeaderDefinitionBuilder.headerDefinition("SLASHSTAR_STYLE_NEWLINE")
            .withFirstLine("/*")
            .withBeforeEachLine(" * ")
            .withEndLine(" */" + System.lineSeparator())
            .withAfterEachLine("")
            .withNoBlankLines()
            .withSkipLinePattern(null)
            .withFirstLineDetectionDetectionPattern("(\\s|\\t)*/\\*.*\$")
            .withLastLineDetectionDetectionPattern(".*\\*/(\\s|\\t)*\$")
            .multiline()
            .noPadLines()
    )
    "mapping"("java", "SLASHSTAR_STYLE_NEWLINE")
    "mapping"("kt", "SLASHSTAR_STYLE_NEWLINE")
    "mapping"("kts", "SLASHSTAR_STYLE_NEWLINE")
    "mapping"("groovy", "SLASHSTAR_STYLE_NEWLINE")
    "include"("**/*.java")
    "include"("**/*.kt")
    "include"("**/*.kts")
    "include"("**/*.groovy")
    "include"("**/*.gradle")
    "ext" {
        setProperty("name", providers.gradleProperty("mod.name").get())
        setProperty("author", providers.gradleProperty("mod.author").get())
        setProperty("year", Year.now().value.toString())
    }
}
