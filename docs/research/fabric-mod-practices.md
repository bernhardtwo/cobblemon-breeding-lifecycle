# Fabric mod practices for a server-only Cobblemon addon (MC 1.21.1)

Research date: 2026-10-21. Target stack: Java 21, Minecraft 1.21.1, Fabric Loader 0.19.2, Fabric API
0.116.11+1.21.1, Fabric Language Kotlin (FLK) 1.13.11+kotlin.2.3.21, Cobblemon 1.7.3+1.21.1. Multi-module Gradle
build (`core`, `cli`, `mod`, `web`).

Source abbreviations:
- `EX:` = [fabric-example-mod, branch 1.21.1](https://github.com/FabricMC/fabric-example-mod/tree/1.21.1) (commit `684b581`, 2026-09-26).
- `CB:` = [Cobblemon, tag 1.7.3](https://gitlab.com/cable-mc/cobblemon/-/tree/1.7.3) (commit `e6fda61`).
- `FAPI:` = [Fabric API, branch 1.21.1](https://github.com/FabricMC/fabric/tree/1.21.1).
- `MOJ:` = [Mojang 1.21.1 server mappings](https://piston-data.mojang.com/v1/objects/03f8985492bda0afc0898465341eb0acef35f570/server.txt).
  Mojang publishes this file through the [1.21.1 version manifest](https://piston-meta.mojang.com/v1/packages/1757c8b1fbfc6b638504e1f6cceb05893f7389d5/1.21.1.json).

## 1. Loom and Gradle

**Versions the 1.21.1 template uses today** ([EX:gradle.properties](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/gradle.properties), [EX:gradle-wrapper.properties](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/gradle/wrapper/gradle-wrapper.properties)):
- `loom_version=1.18-SNAPSHOT`, Gradle `9.7.1`.
- `loader_version=0.19.5`, `fabric_api_version=0.116.17+1.21.1`.

**Plugin ID**
- Use `net.fabricmc.fabric-loom-remap`, which is for obfuscated versions (1.21.11 and older). `fabric-loom` is a legacy alias.
  `net.fabricmc.fabric-loom` (no `-remap`) is only for 26.1+ ([Fabric docs: Loom](https://docs.fabricmc.net/develop/loom/), [Loom 1.14 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.14)).
- The 1.21.1 template applies it as `id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"` ([EX:build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle)).

**JDK and Gradle constraints by Loom version**
| Loom | Gradle | JDK that runs Gradle | Source |
|---|---|---|---|
| 1.14 | ≥ 9.2 | 21 | [1.14 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.14) |
| 1.16 | ≥ 9.4 | 21 | [1.16 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.16), [dev/1.16 build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.16/build.gradle) (`options.release = 21`) |
| 1.17 | built on 9.5 | 21 | [1.17 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.17), [dev/1.17 build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.17/build.gradle) |
| 1.18 | built on 9.7 | **25** | [1.18 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.18) (Java 25 FFM API), [dev/1.18 build.gradle](https://github.com/FabricMC/fabric-loom/blob/dev/1.18/build.gradle) (`options.release = 25`) |

- Loom 1.18 is compiled for Java 25, so the Gradle daemon must run on JDK 25 or newer.
  The mod can still compile to Java 21 with `options.release = 21` ([EX:build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle)).
- If only JDK 21 is available, pin **Loom 1.17** with Gradle ≥ 9.5.
- For reference, Cobblemon builds 1.7.3 with Architectury Loom `1.11-SNAPSHOT` on Gradle 8.14.3 ([CB:libs.versions.toml](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/libs.versions.toml), [CB:gradle-wrapper.properties](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/wrapper/gradle-wrapper.properties)).
  That is a fork, so an addon does not need to match it.

**Multi-module build**
- Declare the plugin once at the root with `apply false`, then apply it only in `mod/build.gradle(.kts)`.
  `core`/`cli` stay plain `java-library` (standard Gradle; Loom configures only the project it is applied to).
- `mod` depends on `core` with a normal `implementation(project(":core"))`, then nests it with `include(project(":core"))`.
  The nested jar becomes a Jar-in-Jar so it ships inside the mod jar.
- Only Loom-to-Loom project dependencies need the `namedElements` configuration ([Fabric docs: Loom](https://docs.fabricmc.net/develop/loom/)).
  Cobblemon's own `fabric` → `common` dependency is an example of this ([CB:fabric/build.gradle.kts](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/build.gradle.kts)).
- Keep Minecraft types out of `core` so that its JUnit tests run without Loom.

## 2. Mappings and the Cobblemon dependency

**Mappings**
- Cobblemon 1.7.3 uses **Mojang official mappings layered with Parchment** `parchment-1.21:2024.07.28` ([CB:base-conventions](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/build-logic/src/main/kotlin/cobblemon.base-conventions.gradle.kts), [CB:gradle.properties](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle.properties)).
  The relevant code:
  ```kotlin
  mappings(loom.layered { officialMojangMappings(); parchment("org.parchmentmc.data:parchment-1.21:2024.07.28@zip") })
  ```
- The 1.21.1 Fabric template also uses `mappings loom.officialMojangMappings()` ([EX:build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle)).
- **Use Mojang mappings, plus Parchment optionally for parameter names.** Names then match Cobblemon's sources (`ServerPlayer`, `CompoundTag`).
  Add the repository `https://maven.parchmentmc.org` ([CB:base-conventions](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/build-logic/src/main/kotlin/cobblemon.base-conventions.gradle.kts)).
- The published Cobblemon jar is in intermediary names, and Loom remaps it to whatever mappings the addon uses (see `cobblemon-addon-java.md` §1). So the mapping choice is about readability, not compatibility.
- Caveat: Fabric API Javadoc and some Fabric docs snippets use Yarn names (`ServerCommandSource`, `TestContext`), for example [FAPI:CommandRegistrationCallback](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-command-api-v2/src/main/java/net/fabricmc/fabric/api/command/v2/CommandRegistrationCallback.java).
  Translate them to Mojang names (`CommandSourceStack`, `GameTestHelper`) using `MOJ:`.

**Cobblemon Maven**
- Coordinates: `com.cobblemon:fabric:1.7.3+1.21.1`.
  - groupId `com.cobblemon`, artifactId = project name `fabric`, version `mod_version+mc_version` ([CB:publish-conventions](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/build-logic/src/main/kotlin/cobblemon.publish-conventions.gradle.kts), [CB:Versioning.kt](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/build-logic/src/main/kotlin/utilities/Versioning.kt)).
- It is published to two repositories:
  - `https://maven.impactdev.net/repository/development/` ([metadata, lists 1.7.3+1.21.1](https://maven.impactdev.net/repository/development/com/cobblemon/fabric/maven-metadata.xml))
  - `https://artefacts.cobblemon.com/releases` ([metadata](https://artefacts.cobblemon.com/releases/com/cobblemon/fabric/maven-metadata.xml))
- The POM declares **no dependencies**, because of `disableDeprecatedPomGeneration` ([CB:publish-conventions](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/build-logic/src/main/kotlin/cobblemon.publish-conventions.gradle.kts)).
  Declare Fabric API and FLK yourself.
- The jar is about 146 MB ([jar](https://maven.impactdev.net/repository/development/com/cobblemon/fabric/1.7.3+1.21.1/fabric-1.7.3+1.21.1.jar)), so the first remap is slow. Cache `~/.gradle` in CI.
- Its runtime needs come from the jar's `fabric.mod.json` (`depends` block below; it also nests `fabric-language-kotlin-1.13.6+kotlin.2.2.20` via `include(libs.fabric.kotlin)` in [CB:fabric/build.gradle.kts](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/build.gradle.kts)):
  ```json
  "depends": {"fabricloader": ">=0.17.2", "fabric-api": ">=0.116.6+1.21.1", "minecraft": "1.21.1", "java": "21"}
  ```
  Our pinned versions satisfy it.

```kotlin
// mod/build.gradle.kts (sketch)
plugins { id("net.fabricmc.fabric-loom-remap") }
repositories {
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://maven.parchmentmc.org")
}
dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.layered { officialMojangMappings(); parchment("org.parchmentmc.data:parchment-1.21:2024.07.28@zip") })
    modImplementation("net.fabricmc:fabric-loader:0.19.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.11+1.21.1")
    modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:1.13.11+kotlin.2.3.21")
    implementation(project(":core")); include(project(":core"))
}
```

## 3. `fabric.mod.json` essentials

**`environment`**
- Accepted values are `"*"`/empty (universal), `"client"` and `"server"`; anything else fails to parse ([fabric-loader V1ModMetadataParser](https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/metadata/V1ModMetadataParser.java)).
- `"server"` means the loader **skips the mod on clients**, including singleplayer.
  That matches a dedicated-server-only addon.

**Entrypoints**
- `main` takes a `ModInitializer`, the common entrypoint ([Fabric docs: project structure](https://docs.fabricmc.net/1.21.1/develop/getting-started/project-structure)).
- `server` takes a `DedicatedServerModInitializer` and runs only on `EnvType.SERVER` ([fabric-loader DedicatedServerModInitializer](https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/api/DedicatedServerModInitializer.java)).
- With `environment: "server"` either works. Prefer `server` so the intent is explicit.

**`depends`**
- Template form: `"fabricloader": ">=…"`, `"minecraft": "~1.21.1"`, `"java": ">=21"`, `"fabric-api": "*"` ([EX:fabric.mod.json](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/src/main/resources/fabric.mod.json)).
- Cobblemon's mod id is `cobblemon` ([CB:fabric.mod.json](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/src/main/resources/fabric.mod.json)).
  FLK's id is `fabric-language-kotlin` (the artifact name in [CB:libs.versions.toml](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/gradle/libs.versions.toml)).
- A Java addon does not load Kotlin classes itself, and Cobblemon already nests FLK.
  Declaring `fabric-language-kotlin` is therefore optional. It is harmless, and it documents the dependency.

```json
{
  "schemaVersion": 1, "id": "breeding_lifecycle", "version": "${version}",
  "environment": "server",
  "entrypoints": { "server": ["dev.example.breeding.BreedingMod"], "fabric-gametest": ["dev.example.breeding.test.BreedingGameTests"] },
  "depends": {
    "fabricloader": ">=0.19.2", "minecraft": "~1.21.1", "java": ">=21",
    "fabric-api": "*", "cobblemon": ">=1.7.3", "fabric-language-kotlin": ">=1.13.6"
  }
}
```
The `fabric-gametest` entrypoint should live in a separate test-only `fabric.mod.json` or source set; see §5.

## 4. Server-only rules and translations

**What to avoid**
- Do not reference `net.minecraft.client.*`, `ClientModInitializer` or `client` mixins.
  The template keeps client code in a split `client` source set ([EX:build.gradle](https://github.com/FabricMC/fabric-example-mod/blob/1.21.1/build.gradle) `splitEnvironmentSourceSets()`).
  A server-only mod should simply not enable it.
- Do not register custom items, blocks, entities or packets that a vanilla or Cobblemon-only client would not understand.
  Stick to vanilla or Cobblemon content plus commands, chat and data.
  (Inference: the client only has Cobblemon's registries, so unknown content would desync.)

**Translations**
- `Component.translatable("key")` is resolved on the **client**, and clients will not have our lang file.
- **Option A, recommended: Server Translations API by Nucleoid** ([README](https://github.com/NucleoidMC/Server-Translations)).
  - Keep using `Component.translatable(...)` and put lang files in `data/<modid>/lang/en_us.json`, `es_es.json`, and so on.
  - The library localizes outgoing text per player, using the language the client reports on join and on change.
  - For 1.21.1 use `xyz.nucleoid:server-translations-api:2.3.1+1.21-pre2` from `https://maven.nucleoid.xyz` ([maven metadata](https://maven.nucleoid.xyz/xyz/nucleoid/server-translations-api/maven-metadata.xml)).
    Its `fabric.mod.json` depends on `minecraft >=1.20.3-` and `fabric-resource-loader-v0` (read from the published jar).
  - Ship it with `modImplementation(include(...))` as the README shows. It also offers `Localization.text(text, player)` / `raw(key, player)` for strings that must be pre-rendered, such as book or sign text ([Localization.java](https://github.com/NucleoidMC/Server-Translations/blob/1.21/api/src/main/java/xyz/nucleoid/server/translations/api/Localization.java)).
- **Option B, no library:** read the locale with `ServerPlayer.clientInformation().language()`.
  Both members exist in 1.21.1 (`MOJ:` `ServerPlayer.clientInformation()` and `ClientInformation.language`).
  Load your own `Map<locale, Map<key, String>>` and send `Component.literal(...)`. This is simpler, but it loses vanilla argument formatting and hover or click composition unless you rebuild it.
- Recommendation: use **Option A**. Keep key-to-string resolution behind a small `Messages` facade in `mod`, so `core` stays MC-free.

## 5. GameTest on 1.21.1

**The API**
- `fabric-gametest-api-v1` (version 2.0.5 in Fabric API 0.116.x, [FAPI:gradle.properties](https://github.com/FabricMC/fabric/blob/1.21.1/gradle.properties)) is part of the `fabric-api` artifact, so no extra dependency is needed.
- Register test classes under the **`fabric-gametest`** entrypoint ([FAPI:gametest package-info](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-gametest-api-v1/src/main/java/net/fabricmc/fabric/api/gametest/v1/package-info.java)).
- Test methods are instance methods that take one context argument and must end by completing the test.
  - Mojang names: `@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)` and `GameTestHelper.succeed()` (`MOJ:` `GameTest.template()`, `GameTestHelper.succeed()`).
  - Yarn names in the Javadoc: `templateName`, `TestContext.complete()`.
- `EMPTY_STRUCTURE = "fabric-gametest-api-v1:empty"` ([FAPI:FabricGameTest](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-gametest-api-v1/src/main/java/net/fabricmc/fabric/api/gametest/v1/FabricGameTest.java)).

**System properties** ([FAPI:FabricGameTestHelper](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-gametest-api-v1/src/main/java/net/fabricmc/fabric/impl/gametest/FabricGameTestHelper.java)):
- `-Dfabric-api.gametest` turns the dedicated server into a test runner that runs every test and exits.
- `-Dfabric-api.gametest.report-file=<path>` writes JUnit XML.
- `fabric-api.gametest.command` enables `/test` (default: client only).

**Loom run config**
- Fabric API's own build uses this config and wires it into `test` ([FAPI:build.gradle](https://github.com/FabricMC/fabric/blob/1.21.1/build.gradle) L416-455):
  ```groovy
  loom { runs { gametest { server(); name "Game Test"
      vmArg "-Dfabric-api.gametest"
      vmArg "-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml"
      runDir "build/gametest" } } }
  test.dependsOn runGametest   // so `./gradlew check` / `build` runs them headless
  ```
  The original inherits `testmodServer`; for us, `server()` plus a gametest source set is enough.
- This is a dedicated server, so CI needs no display. Accept the EULA by writing `eula=true` into `build/gametest/eula.txt` in a `doFirst`. (Inference: standard dedicated-server behaviour.)
- The current docs' `fabricApi { configureTests { … } }` DSL and client gametests target newer Minecraft versions ([docs, latest](https://docs.fabricmc.net/develop/automatic-testing)).
  The 1.21.1 docs page covers only unit tests ([docs 1.21.1](https://docs.fabricmc.net/1.21.1/develop/automatic-testing)), so use the manual `loom.runs` form above.
- Loom 1.17 added a property-based run API and deprecated the old one ([1.17 release](https://github.com/FabricMC/fabric-loom/releases/tag/1.17)). Expect deprecation warnings, not failures.
- Unit tests: `testImplementation "net.fabricmc:fabric-loader-junit:<loader>"` plus `useJUnitPlatform()` ([docs 1.21.1](https://docs.fabricmc.net/1.21.1/develop/automatic-testing)).

**Caveats with Cobblemon on the test server** (inferences from Cobblemon's build; verify on first run)
- Cobblemon bundles GraalVM JS and Molang for its battle engine and loads all species data at startup ([CB:fabric/build.gradle.kts](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/build.gradle.kts) bundles `graal`, `molang`).
  Expect a slow start (tens of seconds) and give the CI job enough memory, around 2 to 4 GB.
- The GameTest server enables all experimental features ([FAPI:package-info](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-gametest-api-v1/src/main/java/net/fabricmc/fabric/api/gametest/v1/package-info.java)).
  World generation therefore differs from production. Use `EMPTY_STRUCTURE` and spawn Pokémon explicitly instead of relying on natural spawns.
- Players in tests are mock players. Party and PC storage tied to a real UUID may need a fake `ServerPlayer`, or you can test the storage-free logic in `core` instead.
- Keep most logic in `core` with plain JUnit, and reserve GameTests for a few integration smoke tests (events fire, NBT persists).

## 6. Commands, permissions, config reload

**Commands**
- Register commands with `CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> …)` from `fabric-command-api-v2` ([FAPI:CommandRegistrationCallback](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-command-api-v2/src/main/java/net/fabricmc/fabric/api/command/v2/CommandRegistrationCallback.java)).
- `environment.includeDedicated` gates dedicated-only commands ([docs 1.21.1: commands](https://docs.fabricmc.net/1.21.1/develop/commands/basics)).
- Vanilla gating uses `.requires(src -> src.hasPermission(2))`. A failing predicate also hides the command from tab completion (same page).

**Permissions**
- Use `me.lucko:fabric-permissions-api:0.3.1`, the version for 1.21.1 or earlier, and declare `"fabric-permissions-api-v0": "*"`. Add it with `include(modImplementation(...))` ([USAGE.md](https://github.com/lucko/fabric-permissions-api/blob/master/USAGE.md)).
- `Permissions.require("mod.cmd.x", 2)` falls back to the vanilla op level when no permissions mod (such as LuckPerms) is installed ([USAGE.md](https://github.com/lucko/fabric-permissions-api/blob/master/USAGE.md)).
- Cobblemon uses the same API with the same fallback pattern ([CB:FabricPermissionValidator.kt](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/src/main/kotlin/com/cobblemon/mod/fabric/permission/FabricPermissionValidator.kt)).
  It does not nest the API in its jar (only FLK is in `META-INF/jars`), so include it ourselves.

**Config hot reload**
- Store the config at `FabricLoader.getInstance().getConfigDir().resolve("<modid>.json")` ([FabricLoader API](https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/api/FabricLoader.java)).
- Load it on `ServerLifecycleEvents.SERVER_STARTING`.
- Reload it in two places: on `END_DATA_PACK_RELOAD`, which fires after vanilla `/reload` ([FAPI:ServerLifecycleEvents](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-lifecycle-events-v1/src/main/java/net/fabricmc/fabric/api/event/lifecycle/v1/ServerLifecycleEvents.java)), and from an explicit `/<modid> reload` command.
- Parse into a new immutable object and validate it before swapping an `AtomicReference`.
  On error, keep the old config and report the error to the command source. (Design advice; no Fabric standard exists.)

## Recommendations for this project
1. Use `net.fabricmc.fabric-loom-remap`, applied **only** in `mod`.
   - Pin **Loom 1.17 with Gradle 9.5+** if CI and dev run JDK 21.
   - Otherwise use 1.18 with Gradle 9.7.x on JDK 25 and keep `options.release = 21`.
2. Use Mojang + Parchment `1.21:2024.07.28` mappings, matching Cobblemon.
3. Depend on `com.cobblemon:fabric:1.7.3+1.21.1` from the impactdev Maven, and add Fabric API and FLK explicitly, since the POM lists no dependencies.
4. In `fabric.mod.json`, set `"environment": "server"`, use the `server` entrypoint, and depend on `cobblemon >=1.7.3`, `fabric-api`, `minecraft ~1.21.1` and `java >=21`.
5. Nest `core` with `include(project(":core"))`. Keep `core` MC-free and unit-test it with plain JUnit.
6. Use Server Translations API `2.3.1+1.21-pre2` (JiJ) for per-player localized text, with lang files in `data/<modid>/lang/`.
7. Use fabric-permissions-api `0.3.1` (JiJ) with an op-level fallback for every command node.
8. Run GameTests through a `loom.runs.gametest` server run with `-Dfabric-api.gametest`, a report file and `test.dependsOn runGametest`. Pre-write `eula.txt`, give CI enough heap, and cache Gradle.
9. Keep the JSON config in `getConfigDir()`, reloaded on `END_DATA_PACK_RELOAD` and by a `reload` subcommand, with validate-then-swap.
