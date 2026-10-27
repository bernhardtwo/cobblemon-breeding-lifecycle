plugins {
    id("blc.java-conventions")
    id("net.fabricmc.fabric-loom-remap")
    id("org.jetbrains.kotlin.jvm")
}

val parchmentVersion = providers.gradleProperty("parchment_version").get()

version = "0.1.0+1.21.1"

base {
    archivesName = "breeding-lifecycle"
}

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://maven.nucleoid.xyz/")
}

val spanishVariants = listOf("es_ar", "es_cl", "es_ec", "es_mx", "es_uy", "es_ve")

val gametest: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

val screenshots: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

sourceSets.test {
    compileClasspath += screenshots.output
    runtimeClasspath += screenshots.output
}

val screenshotsProperties = listOf(
    "breedinglifecycle.screenshots",
    "breedinglifecycle.screenshots.serverConfig",
    "breedinglifecycle.screenshots.output",
)

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
    modImplementation("xyz.nucleoid:server-translations-api:${property("server_translations_version")}") {
        isTransitive = false
    }
    include("xyz.nucleoid:server-translations-api:${property("server_translations_version")}")
    // Nested inside server-translations-api; Loom's dev runtime does not load nested jars.
    modLocalRuntime("xyz.nucleoid:packet-tweaker:0.5.1+24w03b") { isTransitive = false }
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    implementation(project(":core"))
    include(project(":core"))
}

loom {
    mods {
        create("breedinglifecycle") {
            sourceSet(sourceSets.main.get())
        }
        create("breedinglifecycle-gametest") {
            sourceSet(gametest)
        }
        create("breedinglifecycle-screenshots") {
            sourceSet(screenshots)
        }
    }
    runs {
        named("client") {
            source(screenshots)
            runDir("run-client")
            programArgs("--width", "1600", "--height", "900")
            for (name in screenshotsProperties) {
                providers.gradleProperty(name).orNull?.let { vmArg("-D$name=$it") }
            }
        }
        create("gametest") {
            server()
            name("Game Test")
            source(gametest)
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${layout.buildDirectory.get().asFile}/gametest/junit.xml")
            vmArg("-Xmx3G")
            runDir("build/gametest")
            ideConfigGenerated(false)
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
    for (locale in spanishVariants) {
        from("src/main/resources/data/breedinglifecycle/lang/es_es.json") {
            into("data/breedinglifecycle/lang")
            rename { "$locale.json" }
        }
    }
}

tasks.jar {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("LICENSES")) { into("LICENSES") }
}

tasks.named("runGametest") {
    doFirst {
        val dir = layout.buildDirectory.dir("gametest").get().asFile
        dir.mkdirs()
        dir.resolve("eula.txt").writeText("eula=true\n")
    }
}

tasks.test {
    dependsOn("runGametest")
}

val checkDistributedJar by tasks.registering {
    description = "Fails when the distributed jar contains files from the screenshots source set, " +
        "or when a license file, the icon or the bundled Server Translations API jar is missing."
    group = "verification"
    val jar = tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile }
    inputs.file(jar)
    doLast {
        val contents = zipTree(jar.get().asFile)
        val leaked = contents.matching { include("**/*screenshots*/**", "**/*screenshots*") }.files
        if (leaked.isNotEmpty()) {
            throw GradleException("distributed jar contains development files: ${leaked.map { it.name }}")
        }
        val required = mapOf(
            "LICENSE" to "LICENSE",
            "LICENSES/GPL-3.0.txt" to "LICENSES/GPL-3.0.txt",
            "LICENSES/LGPL-3.0.txt" to "LICENSES/LGPL-3.0.txt",
            "assets/breedinglifecycle/icon.png" to "assets/breedinglifecycle/icon.png",
            "META-INF/jars/server-translations-api-*.jar" to "server-translations-api",
        )
        val missing = required.filter { (pattern, _) -> contents.matching { include(pattern) }.isEmpty }.values
        if (missing.isNotEmpty()) {
            throw GradleException("distributed jar is missing: $missing")
        }
    }
}

tasks.check {
    dependsOn(checkDistributedJar)
}
