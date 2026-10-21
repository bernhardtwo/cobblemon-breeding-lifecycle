plugins {
    id("blc.java-conventions")
}

dependencies {
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
    systemProperty("curveVectors", rootProject.file("test-vectors/curve-cases.json").absolutePath)
    inputs.file(rootProject.file("test-vectors/curve-cases.json"))
}
