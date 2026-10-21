plugins {
    id("blc.java-conventions")
    application
}

dependencies {
    implementation(project(":core"))
    implementation("info.picocli:picocli:4.7.7")
}

application {
    mainClass = "io.github.bernhardtwo.breedinglifecycle.cli.Main"
    applicationName = "cli"
}
