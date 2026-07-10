pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.2"
}

stonecutter {
    create(rootProject) {
        version("1.21.1-neoforge", "1.21.1").buildscript = "build.1.21.1-neoforge.gradle"
        version("1.20.1-forge", "1.20.1").buildscript = "build.1.20.1-forge.gradle"
        version("1.20.1-fabric", "1.20.1").buildscript = "build.1.20.1-fabric.gradle"

        vcsVersion = "1.21.1-neoforge"
    }
}

rootProject.name = "modtabs"
