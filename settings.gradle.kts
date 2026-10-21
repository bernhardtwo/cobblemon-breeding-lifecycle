pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
    val loom_version: String by settings
    val kotlin_version: String by settings
    plugins {
        id("net.fabricmc.fabric-loom-remap") version loom_version
        id("org.jetbrains.kotlin.jvm") version kotlin_version
    }
}

rootProject.name = "cobblemon-breeding-lifecycle"

include("core", "cli", "mod")
