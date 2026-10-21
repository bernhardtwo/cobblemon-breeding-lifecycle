plugins {
    base
}

val checkCommentLength = tasks.register<CheckCommentLength>("checkCommentLength") {
    description = "Fails when a Java or TypeScript comment is longer than 200 characters."
    group = "verification"
    sources.from(
        fileTree(rootDir) {
            include("**/src/**/*.java", "web/src/**/*.ts", "web/src/**/*.tsx")
            exclude("**/build/**", "**/node_modules/**", ".gradle/**", "build-logic/**")
        },
    )
}

tasks.check {
    dependsOn(checkCommentLength, gradle.includedBuild("build-logic").task(":test"))
}
