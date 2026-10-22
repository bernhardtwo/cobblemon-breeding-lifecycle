plugins {
    id("blc.java-conventions")
    `java-library`
}

version = "0.1.0"

dependencies {
    api("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
    systemProperty("curveVectors", rootProject.file("test-vectors/curve-cases.json").absolutePath)
    inputs.file(rootProject.file("test-vectors/curve-cases.json"))
    systemProperty("webExportExample", rootProject.file("test-vectors/web-export-example.json").absolutePath)
    inputs.file(rootProject.file("test-vectors/web-export-example.json"))
}
