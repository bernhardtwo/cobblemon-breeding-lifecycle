# Architecture

| Module | Depends on | Role |
|---|---|---|
| `core` | Gson | Loss curve, per-parent roll, seeded RNG, Monte Carlo simulator, JSON config parsing and validation. |
| `cli` | `core`, picocli | Terminal simulator: `curve`, `simulate`, `compare`. |
| `mod` | `core`, Cobblemon, Fabric API | Server-only Fabric mod: Loom, `core` nested in the jar, GameTests in `src/gametest`. |
| `web` | — | Static Vite + TypeScript simulator (milestone 4). |
| `test-vectors/` | — | `curve-cases.json`, checked by the `core` and `web` tests. |

## Build
- `build-logic/` holds the convention plugins: `blc.java-conventions` (Java 21, Spotless, Checkstyle, JUnit) and
  `blc.root-checks` (`checkCommentLength` and the `build-logic` tests wired into `check`).
- `gradle.properties` pins the target modpack versions ([modpack-versions](research/modpack-versions.md)).
- `mod` applies `net.fabricmc.fabric-loom-remap` with Mojang + Parchment mappings, and the Kotlin JVM plugin so Loom
  remaps Cobblemon's Kotlin metadata. `:mod:test` depends on `:mod:runGametest`, a headless GameTest server
  ([0005](decisions/0005-gametest-in-check.md)).
- `./scripts/check.sh` is the full suite. See [0001](decisions/0001-build-toolchain.md) and
  [0002](decisions/0002-comment-length-rule.md).
