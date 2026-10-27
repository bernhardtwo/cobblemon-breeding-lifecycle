# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.0] - 2026-10-27

### Added
- Per-parent loss curve `p(n) = 1 − (1 − p1) · r^(n−1)`, with an optional `maxBreedings`, a separate Ditto curve and
  a breeding counter that each Pokémon carries in its persistent data.
- One-call Java API, `BreedingLifecycle.evaluate(parentA, parentB)`, for a server's own daycare.
- `COLLECT_EGG` adapter for breeding mods that fire Cobblemon's egg event with the real parents.
- Reference daycare to try the curve in game (`/daycare deposit|breed|status|withdraw`), off by default.
- Commands `/breedinglifecycle reload`, `/breedinglifecycle info <1-6>` and `/breedinglifecycle curve`.
- Audit log of every roll in `logs/breeding-lifecycle/audit.jsonl`.
- Server-side translations in English and Spanish, following each client's language; players install nothing.
- Command-line simulator (`curve`, `simulate`, `compare`) and a static web simulator, both in English and Spanish.
