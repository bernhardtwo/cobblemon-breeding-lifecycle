# Publishing on Modrinth and CurseForge

Everything needed to create the project pages by hand. Nothing here is automated: there is no publishing workflow and
no token in the repository.

## Identity
| Field | Value |
|---|---|
| Name | Breeding Lifecycle |
| Suggested slug | `breeding-lifecycle` |
| Summary | An addition to Cobblemon breeding: each parent gets a growing chance of being lost, instead of always being consumed. |
| License | MIT ([LICENSE](../LICENSE)); bundled libraries in [THIRD-PARTY-NOTICES.md](../THIRD-PARTY-NOTICES.md) |
| Source and issues | https://github.com/bernhardtwo/cobblemon-breeding-lifecycle |
| Icon | `mod/src/main/resources/assets/breedinglifecycle/icon.png` (256×256, the same file the jar ships); use a 512×512 copy, kept outside the repository, if a site prefers it |

## Version
| Field | Value |
|---|---|
| File to upload | `mod/build/libs/breeding-lifecycle-0.1.0+1.21.1.jar`, built with `./gradlew :mod:build` |
| Version number | `0.1.0+1.21.1` |
| Release channel | Release |
| Loader | Fabric |
| Minecraft | 1.21.1 |
| Environment | Server: required. Client: unsupported (players install nothing) |
| Required dependencies | Fabric API, Fabric Language Kotlin, Cobblemon `>=1.7.3+1.21.1` |
| Changelog | the `0.1.0` entry of [CHANGELOG.md](../CHANGELOG.md) |

Server Translations API is bundled in the jar, so it is not listed as a dependency.

## Suggested categories
The sites change their category lists; pick the closest ones available when creating the project.
- Modrinth: `game-mechanics` (primary), `mobs`, `utility`.
- CurseForge: Server Utility (primary), Mobs, Miscellaneous; plus the Cobblemon addon tag if the site offers one.

## Gallery
From [docs/screenshots/](screenshots/INDEX.md), in this order:
1. `ingame-daycare-status.png`: one breeding on the default curve, next loss chance `0.4881`.
2. `ingame-loss.png`: both parents lost with `p1 = 1.0`.
3. `ingame-info.png`: `/breedinglifecycle info` of a parent that came back.
4. `ingame-refused.png`: the reference daycare refusing a Pokémon out of its Poké Ball.
5. `cli-curve-en.png`: the default curve table, `E = 2.1981`.
6. `web-1440-light-en-initial.png`: the web simulator.

## Description
Paste the block below as the long description on both sites.

```markdown
# Breeding Lifecycle

**An addition to a breeding system, not a breeding system of its own.** After each breeding, Breeding Lifecycle
decides whether each parent is lost, with a chance that grows with every breeding that parent has done, instead of
always consuming both parents.

## What it needs
The breeding itself comes from one of three places:
- **Your server's daycare**, calling the one-call Java API `BreedingLifecycle.evaluate(parentA, parentB)` after it
  breeds;
- **A breeding mod that fires Cobblemon's `COLLECT_EGG` event** with the real parents, through the bundled adapter
  (`eggEventAdapter.enabled`);
- **The reference daycare** included in the mod (`/daycare`), off by default, to try the curve in game.

## How it works
For a parent's `n`-th breeding the loss chance is `p(n) = 1 − (1 − p1) · r^(n−1)`, reaching `1` at `maxBreedings`
when one is set. With the default curve (`p1 = 0.33`, `r = 0.764`) a parent is lost with a 33 % chance on its first
breeding, then 49 %, 61 %, 70 % and 77 %; on average it gives 2.20 offspring before it is lost, against 1 when both
parents are always consumed. Ditto has its own curve. Each Pokémon carries its own breeding counter, so the chance
follows it through the PC and trades.

## Features
- Server-only: players install nothing. Messages are translated on the server into English or Spanish, following
  each client's language.
- `config/breeding-lifecycle.json` with presets and field-by-field overrides; invalid values are reported with their
  field, value and range. `/breedinglifecycle reload` (or vanilla `/reload`) applies changes.
- `/breedinglifecycle info <1-6>` shows a party Pokémon's breedings and the loss chance of its next breeding;
  `/breedinglifecycle curve` prints the active curves.
- Every roll is written to `logs/breeding-lifecycle/audit.jsonl`.
- The reference daycare honours Cobbreeding's `neutered` flag.

## Requirements
Minecraft 1.21.1, Fabric Loader 0.19.2 or newer, Fabric API, Fabric Language Kotlin and Cobblemon 1.7.3+1.21.1 or
newer, on the server. Java 21.

## Links
Source, documentation, a command-line simulator and a web simulator of the curve:
https://github.com/bernhardtwo/cobblemon-breeding-lifecycle

MIT licensed. Bundles Server Translations API (MIT), which nests Packet Tweaker (LGPLv3).
```
