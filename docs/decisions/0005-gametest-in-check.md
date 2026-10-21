# 0005 · GameTests inside `check`

Status: accepted (2026-10-21)

## Context
The mod's behaviour depends on Cobblemon's `Pokemon`, stores and events, which cannot be built without a running
server. The spec wants those scenarios checked headless by `./scripts/check.sh`.

## Decision
- `mod` has a `gametest` source set with its own test-only `fabric.mod.json` (mod id `breedinglifecycle-gametest`,
  entrypoint `fabric-gametest`), so the shipped jar carries no test classes.
- A Loom run `gametest` starts a dedicated server with `-Dfabric-api.gametest`, writes JUnit XML to
  `mod/build/gametest/junit.xml`, runs in `mod/build/gametest` with `eula.txt` written first, and gets `-Xmx3G`.
  `:mod:test` depends on `:mod:runGametest`, so `./gradlew check` runs every GameTest.
- `mod` also applies the Kotlin JVM plugin (`kotlin_version`, same Kotlin as Fabric Language Kotlin) without any
  Kotlin sources. Loom remaps Kotlin metadata only when that plugin is present; without it Cobblemon's metadata keeps
  intermediary names and Cobblemon crashes at start with `ClassNotFoundException: net.minecraft.class_2960`.

## Consequences
- Measured on an M-series Mac: about 10 s from loader start to `All 1 required tests passed`; the first build
  also remaps the 146 MB Cobblemon jar (about 3 minutes, cached afterwards in `.gradle/loom-cache`).
- The server peaks well under the 3 GB heap; nothing needs a display.
