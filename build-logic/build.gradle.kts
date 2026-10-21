plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.10.4")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    inputs.files(fileTree(rootDir.parentFile) { include("README.md", "README.es.md") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir(rootDir.resolve("../docs")).withPathSensitivity(PathSensitivity.RELATIVE)
}
