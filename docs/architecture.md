# Architecture

| Module | Depends on | Role |
|---|---|---|
| `core` | Gson | Loss curve, per-parent roll, seeded RNG, Monte Carlo simulator, JSON config parsing and validation. |
| `cli` | `core`, picocli | Terminal simulator: `curve`, `simulate`, `compare`. |
| `mod` | `core`, Cobblemon, Fabric API | Server-only Fabric mod (milestone 2). |
| `web` | — | Static Vite + TypeScript simulator (milestone 4). |
| `test-vectors/` | — | `curve-cases.json`, checked by the `core` and `web` tests. |

## Build
- `build-logic/` holds the convention plugins: `blc.java-conventions` (Java 21, Spotless, Checkstyle, JUnit) and
  `blc.root-checks` (`checkCommentLength` and the `build-logic` tests wired into `check`).
- `gradle.properties` pins the target modpack versions ([modpack-versions](research/modpack-versions.md)).
- `./scripts/check.sh` is the full suite. See [0001](decisions/0001-build-toolchain.md) and
  [0002](decisions/0002-comment-length-rule.md).
