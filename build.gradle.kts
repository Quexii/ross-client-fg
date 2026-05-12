plugins {
    alias(libs.plugins.fabricLoom)
    alias(libs.plugins.legacyLooming)
}

val modVersion: String by project
val modGroupId: String by project
val modArtifactId: String by project

group = modGroupId
version = modVersion

val embed: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(embed)

loom {
    runs.named("client") {
        isIdeConfigGenerated = true
        property("devauth.enabled", "true")
        property("devauth.account", "main")

        vmArgs(
            "--add-opens", "java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens", "java.base/sun.misc=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.ref=ALL-UNNAMED"
        )
    }
}

ploceus {
    setIntermediaryGeneration(2)
//    accessWidenerPath.set(file("src/main/resources/${modArtifactId}.accesswidener"))
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.legacyfabric.net/")
    maven("https://maven.glass-launcher.net/babric")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings(ploceus.mcpMappings("stable", "1.8.9", "22"))

    modImplementation(mods.fabric.loader)
    modRuntimeOnly(mods.devauth)
    modRuntimeOnly(mods.netfix)

    embed(libs.skija.types)
    embed(libs.skija.shared)
    embed(libs.skija.windows)
    embed(libs.skija.linux.x64)
    embed(libs.skija.linux.arm64)
    embed(libs.skija.macos.x64)
    embed(libs.skija.macos.arm64)
}

tasks {
    jar {
        manifest {
            attributes("Multi-Release" to "true")
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(embed.files.map {
            zipTree(it)
        })
    }

    processResources {
        inputs.property("mod_version", modVersion)

        filesMatching("**/fabric.mod.json") {
            expand(
                mapOf(
                    "mod_version" to modVersion,
                )
            )
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
}