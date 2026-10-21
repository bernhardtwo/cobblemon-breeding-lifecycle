# Java style and build tooling

Research date: 2026-10-21. Scope: multi-module Gradle build (`core`, `cli`, `mod`) on Java 21, plus a
Vite + TypeScript `web/` folder. Versions were read from Maven Central metadata, Gradle services and
the Fabric Maven on that date. Each heading cites its sources.

## 1. Build DSL: Kotlin DSL

- Gradle has made the Kotlin DSL the default for new builds
  ([Gradle blog](https://blog.gradle.org/kotlin-dsl-is-now-the-default-for-new-gradle-builds)).
- It gives type-safe accessors and full IntelliJ support
  ([Gradle docs](https://docs.gradle.org/current/userguide/kotlin_dsl.html)).
- Caveat: the Fabric template is Groovy (`build.gradle`), so Loom snippets need a small translation
  ([fabric-example-mod 1.21.1](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle)).
- Spotless documents the Kotlin DSL form
  ([Spotless README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)).

## 2. Java 21 conventions worth applying

| Convention | Use it for | Source |
|---|---|---|
| Records | Immutable value types (genes, IVs, breeding results); validate in the compact constructor | [JEP 395](https://openjdk.org/jeps/395) |
| Sealed interfaces | Closed result and outcome hierarchies (`sealed interface BreedResult permits Success, Failure`) | [JEP 409](https://openjdk.org/jeps/409) |
| Switch pattern matching | Exhaustive `switch` over sealed types with no `default`, so adding a subtype breaks the compile | [JEP 441](https://openjdk.org/jeps/441) |
| Record patterns | Deconstruct records inside `switch`/`instanceof` | [JEP 440](https://openjdk.org/jeps/440) |
| `Optional` | Return types only, where "no result" is meaningful. Never a field or parameter, and never `null` | [Optional API note](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Optional.html) |
| Javadoc | Public and protected API only. Skip it for simple, obvious members and for overrides | [Google Java Style 7.3](https://google.github.io/styleguide/javaguide.html#s7.3-javadoc-where-required) |

These are design choices for this repo, not tool-enforced rules:

- Prefer immutability. Return `List.copyOf(...)`-style copies from records.
- Keep packages small and feature-oriented. `core` stays dependency-free, so `cli` and `mod` depend on
  it and never the reverse.

## 3. Formatter: Spotless + palantir-java-format

**Versions (2026-10-21)**

| Tool | Version | Source |
|---|---|---|
| Spotless Gradle plugin | 8.10.4 (released 2026-10-08) | [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.diffplug.spotless) |
| palantir-java-format | 2.102.0 (Spotless's default) | [Maven Central](https://repo1.maven.org/maven2/com/palantir/javaformat/palantir-java-format/maven-metadata.xml), [Spotless CHANGES](https://github.com/diffplug/spotless/blob/main/plugin-gradle/CHANGES.md) |
| google-java-format | 1.37.0 | [Maven Central](https://repo1.maven.org/maven2/com/google/googlejavaformat/google-java-format/maven-metadata.xml) |

**Requirements**

- Spotless 8.1.0 raised the minimum Gradle version to 8.1
  ([CHANGES 8.1.0](https://github.com/diffplug/spotless/blob/main/plugin-gradle/CHANGES.md#810---2025-11-18)).
- It needs JRE 17+ ([README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)).
- It also handles a Gradle 9 deprecation
  ([CHANGES](https://github.com/diffplug/spotless/blob/main/plugin-gradle/CHANGES.md)).
- Result: it works with Gradle 8.x and 9.x.

**palantir-java-format vs google-java-format**

| | palantir-java-format | google-java-format |
|---|---|---|
| Width / indent | 120 columns, 4 spaces ([README](https://github.com/palantir/palantir-java-format)) | 100 columns ([style guide 4.4](https://google.github.io/styleguide/javaguide.html#s4.4-column-limit)), 2 spaces, or 4 with AOSP ([README](https://github.com/google/google-java-format)) |
| Lambdas and chains | Keeps lambda bodies and chains inline more often ([README](https://github.com/palantir/palantir-java-format)) | Splits them more heavily (same comparison) |
| Configurability | Fork of google-java-format ([README](https://github.com/palantir/palantir-java-format)) | "No configurability" of the algorithm ([README](https://github.com/google/google-java-format)) |
| Javadoc formatting | Opt-in through Spotless `formatJavadoc(true)`, needs 2.39.0 or newer ([Spotless README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)) | `formatJavadoc` option ([Spotless README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)) |

**Recommendation: palantir-java-format.**

- Mod and CLI code is lambda-heavy: Fabric event callbacks and picocli.
- The 120-column, 4-space layout matches the Minecraft and Fabric code style.
- The tradeoff, as other projects record it, is weaker editor and tool support outside Spotless
  ([JabRef ADR 0050](https://devdocs.jabref.org/decisions/0050-custom-code-style.html)).

Root `build.gradle.kts` (Kotlin DSL form per the
[Spotless README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)):

```kotlin
plugins {
    id("com.diffplug.spotless") version "8.10.4" apply false
}

subprojects {
    plugins.withId("java") {
        apply(plugin = "com.diffplug.spotless")
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                // Source sets are inferred; keep generated output out explicitly.
                targetExclude("build/**")
                palantirJavaFormat("2.102.0")
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }
    }
}
```

**Spotless and Loom**

- By default, Spotless's Java target comes from the project's Java source sets
  ([README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)).
- Loom's decompiled and remapped Minecraft sources are not in those source sets. `targetExclude("build/**")`
  is a cheap guard for anything generated under `build/`.
- Loom itself is built with Spotless applied and uses `targetExclude` for files it must not touch
  ([fabric-loom build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.18/build.gradle)).
- `spotlessCheck` runs as part of `check`. `spotlessApply` fixes the formatting.
- `ratchetFrom("origin/main")` is optional, for gradual adoption
  ([README](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)).

**web/**

- Spotless can run Prettier, but it needs Node and npm paths configured, and it does not auto-discover
  `.prettierrc` up the directory tree
  ([README, prettier section](https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md)).
- Recommendation: let `web/` keep its own `prettier` and `eslint` npm scripts
  ([Prettier install](https://prettier.io/docs/install)).
- If CI must go through Gradle, add an `Exec` task that runs `npm run lint`.

## 4. Checkstyle

**Version.** Checkstyle 14.3.0 is the latest release
([Maven Central](https://repo1.maven.org/maven2/com/puppycrawl/tools/checkstyle/maven-metadata.xml)). The
Gradle plugin's own default `toolVersion` is older, so pin the version explicitly
([Gradle Checkstyle plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)).

**Does it add value beyond the formatter?** A little. A formatter only rewrites layout. Checkstyle can
catch semantic issues:

- missing braces ([NeedBraces](https://checkstyle.org/checks/blocks/needbraces.html))
- empty catch blocks ([EmptyCatchBlock](https://checkstyle.org/checks/blocks/emptycatchblock.html))
- `equals` without `hashCode` ([EqualsHashCode](https://checkstyle.org/checks/coding/equalshashcode.html))
- utility-class constructors ([HideUtilityClassConstructor](https://checkstyle.org/checks/design/hideutilityclassconstructor.html))
- star imports ([AvoidStarImport](https://checkstyle.org/checks/imports/avoidstarimport.html))
- missing Javadoc on public types ([MissingJavadocType](https://checkstyle.org/checks/javadoc/missingjavadoctype.html))

**Minimal ruleset.** Use the six checks above with `scope=public` on the Javadoc check. Do not enable
whitespace or indentation checks, because they would fight palantir.

**Gradle integration.**

- The plugin reads `config/checkstyle/checkstyle.xml` by default.
- It adds `checkstyleMain` and `checkstyleTest`, and `check` depends on them.
- `maxWarnings` defaults to unlimited, so set it to `0` or use `severity=error`.
- Source: [Gradle Checkstyle plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html).

**Can Checkstyle enforce "no comment > 200 chars"?** Only partially.

- `RegexpSingleline` matches any line in any file type
  ([docs](https://checkstyle.org/checks/regexp/regexpsingleline.html)). A pattern such as
  `(//|/\*|^\s*\*).{200,}` works per line, but it cannot tell `//` inside a string (for example a URL)
  from a real comment.
- `RegexpSinglelineJava` can only *ignore* comments. It cannot target them
  ([docs](https://checkstyle.org/checks/regexp/regexpsinglelinejava.html)).
- `LineLength` measures whole lines, not comments
  ([docs](https://checkstyle.org/checks/sizes/linelength.html)).
- Checkstyle tasks run on Java source sets only, so `web/` would need a separate mechanism.

**Recommendation: one custom Gradle task.**

- Register `checkCommentLength` in the root build, or in a `build-logic` convention plugin.
- It walks `**/src/**/*.java` and `web/src/**/*.{ts,tsx}`, excluding `build/` and `node_modules/`.
- For each file it runs a small state machine with three states: in string or template literal, in
  `//`, and in `/* ... */`.
- It measures each comment's text and fails with `file:line` when the length exceeds 200.
- Javadoc (`/** */`) is covered as a block comment.
- Hook it in with the `base` plugin at the root, which provides `check`
  ([Gradle base plugin](https://docs.gradle.org/current/userguide/base_plugin.html)):

```kotlin
plugins { base }
val checkCommentLength by tasks.registering(CommentLengthCheck::class) {   // class in build-logic
    sources.from(fileTree(".") {
        include("**/src/**/*.java", "web/src/**/*.ts", "web/src/**/*.tsx")
        exclude("**/build/**", "**/node_modules/**")
    })
    maxLength = 200
}
tasks.named("check") { dependsOn(checkCommentLength) }
```

**Decision for the repo:** decide whether "200 characters" applies per physical line or per whole
comment. A whole `/** */` block of more than 200 characters is common on public API.

## 5. Tests and wrapper versions

| Item | Version | Notes and source |
|---|---|---|
| JUnit | 6.1.3 (latest); 5.14.4 is the last 5.x | Both from [Maven Central](https://repo1.maven.org/maven2/org/junit/junit-bom/maven-metadata.xml). JUnit 6 needs Java 17+ at runtime ([user guide](https://docs.junit.org/current/user-guide/)). Use `junit-bom` + `junit-jupiter`, plus `junit-platform-launcher` as `testRuntimeOnly` ([build support](https://docs.junit.org/current/running-tests/build-support.html)). |
| AssertJ | 3.27.7 | Latest stable; 4.0.0-M1 is a milestone ([Maven Central](https://repo1.maven.org/maven2/org/assertj/assertj-core/maven-metadata.xml)) |
| picocli | 4.7.7 | [Maven Central](https://repo1.maven.org/maven2/info/picocli/picocli/maven-metadata.xml) |
| Gradle (current) | 9.8.2 | [services.gradle.org](https://services.gradle.org/versions/current). Gradle 9 runs on JVM 17-27, and Java 25 has been supported since 9.1 ([compatibility](https://docs.gradle.org/current/userguide/compatibility.html)). |

**Loom correction: the "Loom 1.7-1.10" range is outdated.** The official 1.21.1 template now uses:

| Setting | Value |
|---|---|
| `loom_version` | `1.18-SNAPSHOT` |
| Plugin id | `net.fabricmc.fabric-loom-remap` |
| Gradle wrapper | 9.7.1 |
| Java | `options.release = 21` |

Sources: [gradle.properties](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/gradle.properties),
[build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle),
[wrapper](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/gradle/wrapper/gradle-wrapper.properties).

- Loom 1.14 introduced the `fabric-loom-remap` id for obfuscated versions (1.21.11 and older) and
  requires Gradle 9.2 ([releases](https://github.com/FabricMC/fabric-loom/releases)).
- Loom 1.16 raised the minimum Gradle version to 9.4 ([releases](https://github.com/FabricMC/fabric-loom/releases)).
- Latest published patch versions: 1.17.21, 1.18.4, 1.19.2
  ([Fabric Maven](https://maven.fabricmc.net/net/fabricmc/fabric-loom-remap/net.fabricmc.fabric-loom-remap.gradle.plugin/maven-metadata.xml)).
- **JDK warning:** Loom 1.18 and 1.19 are compiled with `options.release = 25`, while 1.16 and 1.17 use
  21 ([1.18 build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.18/build.gradle),
  [1.17 build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.17/build.gradle)).
- Loom 1.18 also adopted Java 25's FFM API ([1.18 notes](https://github.com/FabricMC/fabric-loom/releases/tag/1.18)).
- Inference, not stated by Fabric: Gradle must run on JDK 25 for Loom 1.18 or newer. Mod code still
  targets release 21.

## 6. Conventional Commits

Format: `type(scope): description`, with `feat`, `fix`, and `BREAKING CHANGE`/`!`
([spec v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/)). Suggested scopes: `core`, `cli`, `mod`,
`web`, `build`.

## Recommendations for this project

1. **Build DSL:** Kotlin DSL, with shared settings in a `build-logic` included build (convention
   plugins for java, spotless and checkstyle).
2. **Gradle and Loom:** wrapper 9.7.1, matching the official 1.21.1 template, with
   `net.fabricmc.fabric-loom-remap` at 1.18.4.
   - Run Gradle on JDK 25.
   - Compile every module with `options.release = 21`, as the template does.
   - If JDK 25 is unwanted, pin Loom 1.17.21 instead (built for Java 21; Loom 1.16 and newer need Gradle 9.4 or newer).
3. **Formatting:** Spotless 8.10.4 with `palantirJavaFormat("2.102.0")`, `removeUnusedImports()` and
   `targetExclude("build/**")` on all Java modules. CI runs `spotlessCheck` through `check`.
4. **web/:** keep its own Prettier and ESLint through npm scripts. Do not run Prettier through Spotless.
5. **Checkstyle 14.3.0:** the minimal six-check ruleset above, with `maxWarnings = 0`.
   - Use `MissingJavadocType` with public scope to enforce "Javadoc on public API".
   - Leave layout checks to the formatter.
6. **Comment length:** a custom `checkCommentLength` Gradle task, a comment-aware scanner over
   `*.java` and `web/**/*.ts(x)`, wired into the root `check`. Do not rely on the Checkstyle regex,
   which gives false positives on `//` in strings and does not cover TypeScript. Settle the
   per-line vs per-comment interpretation first.
7. **Tests:** JUnit 6.1.3 (BOM) + AssertJ 3.27.7 in `core` and `cli`. Keep `mod` logic thin so most
   tests live in pure-Java `core`.
8. **Code style:** records and sealed interfaces with exhaustive pattern `switch` for domain results,
   `Optional` only as a return type, and immutable collections.
9. **Commits:** Conventional Commits with module scopes.
