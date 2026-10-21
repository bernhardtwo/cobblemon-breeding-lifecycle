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

## The mod
Root package `io.github.bernhardtwo.breedinglifecycle`:

| Package | Contents |
|---|---|
| `api` | `BreedingLifecycle.evaluate`, `BreedingResult`, `ParentResult`: the only public API ([integration](integration.md)). |
| `mod` | `BreedingLifecycleMod` (server entrypoint), `LifecycleService` (evaluation), `BreedingCounter` (counter in `persistentData`). |
| `mod.config` | `ModConfig` (read, write default, validate switches), `ModSettings`, `ConfigHolder` (validate-then-swap reload). |
| `mod.audit` | `AuditEntry` and `AuditLog`: JSON lines written on the `breedinglifecycle-audit` thread. |
| `mod.command` | `/breedinglifecycle reload, info, curve` and `/daycare deposit, breed, status, withdraw`; text in `data/breedinglifecycle/lang/` ([0007](decisions/0007-server-side-translations.md)). |
| `mod.adapter` | `EggEventAdapter` on `COLLECT_EGG` ([0008](decisions/0008-collect-egg-adapter.md)). |
| `mod.daycare` | Reference daycare: `Compatibility`, `Offspring`, `DaycareData` (one `SavedData`), `Daycare` and `BreedOutcome` ([0009](decisions/0009-reference-daycare.md)). |

**Start-up.** `onInitializeServer` opens `config/breeding-lifecycle.json` (writing the default if missing), creates
the audit log under `logs/breeding-lifecycle/`, installs the `LifecycleService`, subscribes the adapter and registers
the command. `SERVER_STARTED` attaches the server, `END_DATA_PACK_RELOAD` reloads the config and `SERVER_STOPPED`
drains the audit log.

**`evaluate(a, b, source)`.** On the server thread only: read the effective curves from the current settings, pick
the `ditto` curve for a `cobblemon:ditto` parent, roll each parent with `LossDecider` on its own counter, increment
both counters (then `onChange(null)`), queue two audit entries and return the result. Removing lost parents is the
caller's job; the adapter does it for `COLLECT_EGG`.

**Atomicity.** Stores and `SavedData` are snapshotted on the server thread, so one breeding in one task touching one
persistent store is crash-safe ([save-timing](research/save-timing.md), [0006](decisions/0006-breeding-atomicity.md)).

**Tests.** JUnit in `mod/src/test` covers config and audit without Minecraft; GameTests in `mod/src/gametest` cover
everything that needs Cobblemon: counter, `evaluate`, commands and translations, the adapter, the reference daycare.
