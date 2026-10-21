# cobblemon-breeding-lifecycle

Breeding with a growing, per-parent chance of losing each parent, instead of always consuming both.
A pure-Java rules core, a CLI simulator, a server-only Fabric mod for Cobblemon, and a static web simulator.
Generic for any Cobblemon server: no server or network names in code, ids or package names.

## Stack and pinned versions
Minecraft 1.21.1 · Java 21 · Cobblemon `1.7.3+1.21.1` · Fabric Loader 0.19.2 · Fabric API `0.116.11+1.21.1`
· Fabric Language Kotlin `1.13.11+kotlin.2.3.21` — source: [modpack-versions](docs/research/modpack-versions.md).
Pinned in `gradle.properties`; never bump them without updating that doc.
Gradle 9.7.1 (Kotlin DSL) · Loom `net.fabricmc.fabric-loom-remap` 1.17.x (runs on JDK 21) · Mojang + Parchment mappings
· picocli · JUnit · AssertJ · Spotless + palantir-java-format · Checkstyle · Vite + TypeScript for `web/`.
See [java-style-tooling](docs/research/java-style-tooling.md) and [fabric-mod-practices](docs/research/fabric-mod-practices.md).

## Modules
| Path | What |
|---|---|
| `core` | Rules in plain Java. No Minecraft, no third-party runtime deps. |
| `cli` | picocli simulator on `core`. |
| `mod` | Server-only Fabric mod on `core` + Cobblemon; includes the reference daycare. Only module with Loom. |
| `web` | Static Vite + TypeScript simulator, no backend. |
| `test-vectors/` | `curve-cases.json`, shared by `core` and `web` tests. |

## Commands
- `./scripts/check.sh` — the full suite: `./gradlew check` (tests, Spotless, Checkstyle, comment-length check, GameTests)
  then `npm --prefix web ci && npm --prefix web test && npm --prefix web run build`.
- `./gradlew spotlessApply` — run before every commit.

## Fabric and Cobblemon rules
- `fabric.mod.json`: `"environment": "server"`, `server` entrypoint; players install nothing.
- No mixins unless no event or API exists. Touch Pokémon only on the server thread.
- Breeding counter lives in `Pokemon.getPersistentData()` under a namespaced key; mutate in place, then `pokemon.onChange(null)`.
- Call Kotlin APIs with every argument (no `$default`), e.g. `species.create(1)`; use block lambdas for `subscribe`.
- Player-facing text via `Component.translatable` with server-side translations (es, en).
- Neutered: honour Cobbreeding's `neutered` boolean in `persistentData`.
- Details: [cobblemon-addon-java](docs/research/cobblemon-addon-java.md), [breeding-hooks](docs/research/breeding-hooks.md).

## Repo rules
- Clean, self-explanatory code. No comment over 200 characters (whole comment, markers stripped); no long blocks,
  no line-by-line commenting. Brief Javadoc on public API only. Enforced by `checkCommentLength` in `check`.
- Commits: English Conventional Commits (`feat(core): …`, `docs: …`, `test(cli): …`). No `Co-Authored-By`,
  no session line, no mention of any AI assistant. Never `--amend`, force push or `--no-verify`.
- Docs in English under `docs/`, updated in the same commit as the change: `architecture.md`, `formula.md`,
  `configuration.md`, `integration.md`, `cli.md`, `web.md`, `research/`, `decisions/` (short ADR per own decision).
- i18n: every user-facing string in es and en (CLI `--lang`, mod per client locale, web toggle and `?lang=`).
- Screenshots committed in `docs/screenshots/`, described in `docs/screenshots/INDEX.md`.
- GitHub: only push `main`. No PRs, issues or settings changes.
