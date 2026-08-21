plugins {
    id("dev.kikugie.stonecutter")
}

val registeredVersions = providers.gradleProperty("stonecutter_enabled_versions")
    .get().split(',').map(String::trim).filter(String::isNotEmpty)
check(registeredVersions.isNotEmpty()) { "stonecutter_enabled_versions must contain at least one version" }
val configuredVcsVersion = providers.gradleProperty("stonecutter_vcs_version")
    .orNull
    ?.trim()
    ?.takeIf(String::isNotEmpty)
val activeVersion = configuredVcsVersion ?: registeredVersions.first()
check(activeVersion in registeredVersions) {
    "stonecutter_vcs_version=$activeVersion must also be listed in stonecutter_enabled_versions"
}

// The VCS node is explicitly configurable; fall back to the first registered
// version so focused checkouts remain usable when the property is omitted.
stonecutter active activeVersion
