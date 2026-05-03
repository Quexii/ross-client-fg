pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
        maven("https://maven.ornithemc.net/snapshots")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("mods") {
            from(files("gradle/mods.versions.toml"))
        }
    }
}

rootProject.name = "ross-client"