plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.kikugie.dev/releases")
    maven("https://maven.kikugie.dev/snapshots")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    implementation("dev.kikugie:stonecutter:0.7.11")
    implementation("net.fabricmc:fabric-loom:1.15.3")
    implementation("dev.kikugie:fletching-table:0.1.0-alpha.22")
    implementation("net.minecraftforge:jarjar-gradle:0.2.3")
    implementation("net.minecraftforge:renamer-gradle:1.1.2")
    implementation("com.github.hierynomus.license:com.github.hierynomus.license.gradle.plugin:0.16.1")
}
