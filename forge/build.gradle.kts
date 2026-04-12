plugins {
    alias(libs.plugins.modDevGradle)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://maven.squiddev.cc")
    maven("https://maven.parchmentmc.org")
}

neoForge {
    version = libs.versions.neoforge.get()

    parchment {
        minecraftVersion = libs.versions.parchmentMc.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    runs {
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }

    mods {
        register("ccgraphics") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        java.srcDir(rootProject.file("common/src/main/java"))
        resources.srcDir(rootProject.file("common/src/main/resources"))
        java.srcDir(rootProject.file("common/src/client/java"))
    }
}

dependencies {
    implementation(libs.cc.tweaked.forge)
    compileOnly(libs.cc.tweaked.core)

    // NightConfig - bundled at runtime by NeoForge (transitive via cc-tweaked-forge)
}

base {
    archivesName.set("ccgraphics-${properties["mcVersion"]}-forge")
    version = properties["modVersion"] as String
}

tasks.processResources {
    inputs.property("version", project.property("modVersion"))

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.property("modVersion"))
    }
}
