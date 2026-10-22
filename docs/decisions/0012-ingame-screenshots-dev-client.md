# 0012 · In-game screenshots from a development client mod

Status: accepted (2026-10-22)

## Context
The in-game screenshots were taken by hand: driving the real client means sending keys with `osascript` to whatever
window is in front, which is unsafe on a desktop someone is using. They should be taken with no one at the keyboard.

## Decision
- No operating-system automation: no `osascript`, `screencapture`, keys or clicks outside the game.
- A client-only Fabric mod in its own source set, `mod/src/screenshots`, loaded only by `runClient` and active only
  with `-Dbreedinglifecycle.screenshots=true`. It sends the chat commands, waits for each reply, and captures with
  Minecraft's `Screenshot.grab`.
- `checkDistributedJar` in `check` keeps it out of the distributed jar.
- The fourth screenshot is the `deposit` refusal of a Pokémon out of its Poké Ball: the client sends it out with
  Cobblemon's `SendOutPokemonPacket`, as the send-out key does. `withdraw` in battle was not used: a battle needs a
  second player or a wild Pokémon in reach, which is harder to make repeatable. The chosen one passed three
  consecutive `auto` runs.
- Each scene empties the daycare and the party and gives a new pair, so a run does not depend on earlier ones.

## Consequences
- `scripts/ingame-screenshots.sh auto` regenerates all four in about five minutes, and fails with the missing
  reply when something changes.
- The screenshots show Cobblemon and this mod alone, in English; the pack's other mods and Spanish stay with
  [live-test](../live-test.md).
