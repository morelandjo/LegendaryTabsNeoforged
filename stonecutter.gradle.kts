@file:OptIn(dev.kikugie.stonecutter.StonecutterExperimentalAPI::class)

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.13.6" apply false
    id("net.minecraftforge.gradle") version "[6.0,6.2)" apply false
    id("org.spongepowered.mixin") version "0.7.+" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
    // Defines `fabric` / `forge` / `neoforge` constants for `//? loader {` guards,
    // derived from the entry id suffix (e.g. "1.20.1-forge" -> forge).
    constants.match(current.project.substringAfterLast('-'), "fabric", "forge", "neoforge")
}

tasks.register("runActiveClient") {
    group = "stonecutter"
    description = "Run client for the active Stonecutter version"
    dependsOn(stonecutter.current!!.project + ":runClient")
}

tasks.register("chiseledBuild") {
    group = "build"
    description = "Build every supported ModTabs target"
    dependsOn(
        ":1.21.1-neoforge:build",
        ":1.20.1-forge:build",
        ":1.20.1-fabric:build",
    )
}

tasks.register("integrationTest") {
    group = "verification"
    description = "Run contract and Minecraft-backed integration tests for every target"
    dependsOn(
        ":1.21.1-neoforge:test",
        ":1.20.1-forge:test",
        ":1.20.1-fabric:test",
    )
}
