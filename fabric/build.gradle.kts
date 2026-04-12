plugins {
    alias(libs.plugins.fabric.loom)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://maven.squiddev.cc")
    maven("https://maven.parchmentmc.org") {
        name = "Parchment"
        content {
            includeGroup("org.parchmentmc.data")
        }
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/ccgraphics.accesswidener"))

    mixin {
        defaultRefmapName.set("ccgraphics-refmap.json")
    }

    runs {
        named("client") {
            configName = "Fabric Client"
            ideConfigGenerated(true)
        }
        named("server") {
            configName = "Fabric Server"
            ideConfigGenerated(true)
        }
    }
}

sourceSets {
    main {
        java.srcDir(rootProject.file("common/src/main/java"))
        java.srcDir(rootProject.file("common/src/client/java"))
        resources.srcDir(rootProject.file("common/src/main/resources"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["mcVersion"]}")
    mappings(
        loom.layered {
            officialMojangMappings()
            parchment(
                project.dependencies.create(
                    group = "org.parchmentmc.data",
                    name = "parchment-${libs.versions.parchmentMc.get()}",
                    version = libs.versions.parchment.get(),
                    ext = "zip",
                ),
            )
        },
    )

    modImplementation("net.fabricmc:fabric-loader:${libs.versions.fabric.loader.get()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${libs.versions.fabric.api.get()}")

    modImplementation(libs.cc.tweaked.fabric)
    compileOnly(libs.cc.tweaked.core)

    // NightConfig - bundled at runtime by CC:T's jar-in-jar
    compileOnly(libs.nightConfig.core)
    compileOnly(libs.nightConfig.toml)
}

base {
    archivesName.set("ccgraphics-${properties["mcVersion"]}-fabric")
    version = properties["modVersion"] as String
}

tasks.processResources {
    inputs.property("version", project.property("modVersion"))

    filesMatching("fabric.mod.json") {
        expand("version" to project.property("modVersion"))
    }
}
