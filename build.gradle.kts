//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
//import dev.architectury.pack200.java.Pack200Adapter
//import gg.essential.gradle.util.noServerRunConfigs
//import org.gradle.api.tasks.JavaExec
//import org.gradle.jvm.toolchain.JavaLanguageVersion
//import org.gradle.jvm.toolchain.JavaToolchainService
//
//plugins {
//    id("java")
//    id("gg.essential.multi-version")
//    id("gg.essential.defaults")
//    id("com.github.johnrengelman.shadow")
//}
//
//version = "1.0.0"
//base.archivesName.set("ross-forge")
//
//val skijaVersion = "0.116.8"
//
//val shade: Configuration by configurations.creating {
//    isCanBeResolved = true
//    isCanBeConsumed = false
//}
//
//val embed: Configuration by configurations.creating
//configurations.implementation.get().extendsFrom(embed)
//
//val devAuthMod: Configuration by configurations.creating {
//    isCanBeResolved = true
//    isCanBeConsumed = false
//}
//
//repositories {
//    mavenCentral()
//    maven("https://repo.spongepowered.org/repository/maven-public/")
//    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
//}
//
//dependencies {
//    embed(libs.mixins)
//    annotationProcessor(variantOf(libs.mixins.annotations) { classifier("processor") })
//
//    // for whatever reason runtimeOnly doesnt work so a workaround
//    devAuthMod(mods.devauth)
//
//    embed(libs.skija.types)
//    embed(libs.skija.shared)
//    embed(libs.skija.windows)
//    embed(libs.skija.linux.x64)
//    embed(libs.skija.linux.arm64)
//    embed(libs.skija.macos.x64)
//    embed(libs.skija.macos.arm64)
//}
//
//val javaToolchains = extensions.getByType(JavaToolchainService::class.java)
//
//tasks.named<JavaExec>("runClient") {
//    javaLauncher.set(javaToolchains.launcherFor {
//        languageVersion.set(JavaLanguageVersion.of(8))
//    })
//
//    jvmArgs("-noverify")
//
//    doFirst {
//        val modsDir = File(rootDir, "run/mods")
//        if (!modsDir.exists()) {
//            modsDir.mkdirs()
//        }
//
//        devAuthMod.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
//            val dest = modsDir.resolve(artifact.file.name)
//            artifact.file.copyTo(dest, overwrite = true)
//        }
//    }
//}
//
//tasks {
//    register<Copy>("updateLauncher") {
//        description = "Copies the built jar to the launcher directory for testing, requires you to set the path."
//        group = "ross"
//
//        val launcherPath = System.getenv("ROSS_LAUNCHER_PATH")
//
//        from(named("remapJar"))
//        into(launcherPath ?: temporaryDir.path)
//
//        onlyIf {
//            val isSet = System.getenv("ROSS_LAUNCHER_PATH") != null
//            if (!isSet) {
//                println("ROSS_LAUNCHER_PATH environment variable not set, skipping updateLauncher task.")
//            }
//            isSet
//        }
//    }
//
//    named("build") {
//        finalizedBy("updateLauncher")
//    }
//
//    jar {
//        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        from(embed.files.map { zipTree(it) })
//
//        manifest {
//            attributes(
//                "ModSide" to "CLIENT",
//                "FMLCorePlugin" to "eu.shoroa.ross.mixins.plugin.RossCoreMod",
//                "FMLCorePluginContainsFMLMod" to "No (but actually yes)",
//                "ForceLoadAsMod" to true,
//                "MixinConfigs" to "ross.mixins.json",
//                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
//                "TweakOrder" to 0
//            )
//        }
//
//        exclude("META-INF/versions/**")
//        exclude("**/module-info.class")
//        exclude("**/package-info.class")
//        exclude("LICENSE.txt")
//        exclude("META-INF/LICENSE.txt")
//        exclude("META-INF/NOTICE.txt")
//        exclude("META-INF/*.SF")
//        exclude("META-INF/*.DSA")
//        exclude("META-INF/*.RSA")
//
//        archiveBaseName.set("ross-forge")
//    }
//
//    processResources {
//        val projectVersion = project.provider { project.version.toString() }
//        val mcVersion = project.provider { (findProperty("mcVersionStr") as String?) ?: "1.8.9" }
//
//        inputs.property("project_version", projectVersion)
//        inputs.property("mc_version", mcVersion)
//
//        filesMatching("mcmod.info") {
//            expand(
//                mapOf(
//                    "version" to projectVersion.get(),
//                    "mcVersionStr" to mcVersion.get()
//                )
//            )
//        }
//    }
//}
//
//loom {
//    noServerRunConfigs()
//
//    runConfigs {
//        named("client") {
//            isIdeConfigGenerated = true
//        }
//    }
//
//    mixin {
//        defaultRefmapName.set("ross.mixins.refmap.json")
//        add(sourceSets.main.get(), "ross.mixins.refmap.json")
//    }
//
//    launchConfigs.named("client") {
//        arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
//        property("loader.coreMods.load", "eu.shoroa.ross.mixins.plugin.RossCoreMod")
//        property("devauth.enabled", "true")
//        property("devauth.account", "main")
//        property("mixin.hotSwap", "true")
//    }
//
//    forge {
//        pack200Provider.set(Pack200Adapter())
//        mixinConfig("ross.mixins.json")
//    }
//}

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