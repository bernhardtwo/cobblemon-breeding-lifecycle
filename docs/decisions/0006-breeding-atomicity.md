# 0006 · Breeding atomicity: one tick, one `SavedData`

Status: accepted (2026-10-21)

## Context
S8 assumed (UNVERIFIED in the spec) that saves happen on the server thread between ticks, so a breeding done in
one tick is atomic. [save-timing](../research/save-timing.md) checks it against Cobblemon 1.7.3 and Minecraft 1.21.1.

## Decision
The assumption holds per store: Cobblemon serializes its stores at `START_SERVER_TICK` and Minecraft writes
`SavedData` after `tickChildren`, both on the server thread. The reference daycare (milestone 3) therefore keeps
the design of S8: generate the offspring, apply both counters and drop the lost parents in one server-thread task,
touching only the daycare's own `SavedData`, and call `setDirty()` once at the end. No idempotent breeding ids.

## Consequences
- Parents live in the `SavedData` while deposited, so their counters are written with the offspring.
- `deposit` and `withdraw` move a Pokémon between a Cobblemon store and the `SavedData`, which are saved at
  different moments; a crash between those saves can duplicate or drop that one Pokémon. That is a transfer, not a
  breeding, and milestone 3 documents it.
- `docs/integration.md` tells third-party daycares the same rule: one task, one persistent store per breeding.
