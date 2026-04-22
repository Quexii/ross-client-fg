pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.minecraftforge.net")
    }

    plugins {
        val egtVersion = "0.1.0"
        id("gg.essential.multi-version.root") version egtVersion
        id("gg.essential.multi-version.api-validation") version egtVersion
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("mods") {
            from(files("gradle/mods.versions.toml"))
        }
    }
}

include(":1.8.9-forge")
project(":1.8.9-forge").apply {
    projectDir = file("versions/1.8.9-forge")
    buildFileName = "../../build.gradle.kts"
}

rootProject.buildFileName = "root.gradle.kts"

rootProject.name = "ross-client"