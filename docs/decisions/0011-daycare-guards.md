# 0011 · Daycare guards: nothing stuck, nothing pulled from the world

Status: accepted (2026-10-21)

## Context
Switching `referenceDaycare.enabled` off used to block every subcommand, `withdraw` included, so parents and
offspring stayed in `DaycareData` until it was switched back on. `deposit` recalled a Pokémon that was out of its
Poké Ball, and `withdraw` ignored battles.

## Decision
- When disabled, `deposit` and `breed` are refused; `status` and `withdraw` keep working.
- `deposit` refuses a Pokémon that is out of its Poké Ball (`sent_out`) instead of recalling it.
- `withdraw` is refused while the player is in a battle (`withdraw_in_battle`).
- Every refusal changes neither the party nor the daycare.

## Consequences
- A server can switch the daycare off at any time without trapping anyone's Pokémon.
- Players recall a Pokémon themselves before depositing it, and finish a battle before withdrawing.
- This replaces the rule in [configuration](../configuration.md) that switching it off also blocks `withdraw`.
