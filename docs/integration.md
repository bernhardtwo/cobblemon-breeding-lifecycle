# Integration

How a server's own daycare, or another mod, applies the breeding lifecycle. The mod must be installed on the
server; players install nothing.

## The call
```java
import io.github.bernhardtwo.breedinglifecycle.api.BreedingLifecycle;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;

BreedingResult result = BreedingLifecycle.evaluate(parentA, parentB);
```

`evaluate` rolls the loss of each parent independently, with its own breeding number and its own curve (a Ditto
uses the `ditto` curve, its partner the main one), adds one breeding to **both** counters, writes two audit lines
with source `api`, and returns:

| Field | Meaning |
|---|---|
| `parentA()`, `parentB()` | One `ParentResult` per parent, in the order passed. |
| `ParentResult.pokemon()` | The parent's UUID. |
| `n()` | The breeding number just rolled (1 for a parent that never bred). |
| `p()` | The loss chance used, `p(n)` of its curve. |
| `roll()` | The uniform roll in `[0, 1)`; the parent is lost when `roll < p`. |
| `survives()` | `false` when the parent is lost. |

## Contract
- **Server thread only.** Called from any other thread, or before the server has started, `evaluate` throws
  `IllegalStateException` and changes nothing.
- **The caller removes lost parents.** `evaluate` never touches stores; for each `survives() == false`, remove that
  Pokémon from wherever your daycare keeps it.
- **Breed first, then evaluate.** The offspring is always generated; the loss is rolled after it.
- **One task, one store.** Generate the offspring, call `evaluate` and remove the lost parents in the same
  server-thread task (one tick), and keep parents and offspring in a single persistent store, such as one
  `SavedData`. Cobblemon's party and PC are saved at different moments from `SavedData`, so a breeding that edits
  both can be half-saved by a crash. See [save-timing](research/save-timing.md) and
  [0006](decisions/0006-breeding-atomicity.md).

## Example: a minimal daycare
```java
public final class MyDaycare extends SavedData {
    private final List<Pokemon> parents = new ArrayList<>();
    private final List<Pokemon> eggs = new ArrayList<>();

    /** Runs on the server thread, e.g. from a command or a tick handler. */
    public void breed() {
        Pokemon mother = parents.get(0);
        Pokemon father = parents.get(1);
        eggs.add(mother.getSpecies().create(1));
        BreedingResult result = BreedingLifecycle.evaluate(mother, father);
        if (!result.parentB().survives()) {
            parents.remove(father);
        }
        if (!result.parentA().survives()) {
            parents.remove(mother);
        }
        setDirty();
    }
}
```

## The counter
Each Pokémon carries its own count in `Pokemon.getPersistentData()`: compound `breedinglifecycle`, integer
`breedings`. It is saved in party and PC, travels with trades and is visible to clients. Any numeric tag is read (Cobblemon's JSON
storage brings small numbers back as bytes); a missing, non-numeric or negative value reads as 0, so Pokémon from before the mod was installed start at their first breeding. The counter
never resets; a Pokémon at or past `maxBreedings` is lost on its next breeding.

## Breeding mods that fire COLLECT_EGG
With `eggEventAdapter.enabled` (default `false`), the mod listens to Cobblemon's `COLLECT_EGG` event:

- When both parents are in a real party or PC, it calls `evaluate` with source `egg-event` and removes every lost
  parent from its store. A parent in a pasture is untethered first.
- When either parent is in no store, as with Cobbreeding's blank placeholder parents, the event is ignored and a
  warning is logged; nothing is counted or audited.
- In practice this covers Daycare+ 1.4.2 while the pasture owner is online. Cobbreeding, and Daycare+ eggs produced
  while the owner is offline, cannot be covered without mixins. See [0008](decisions/0008-collect-egg-adapter.md).

## Reference daycare
The mod ships a small daycare of its own, `/daycare`, as a test bench for the curve. It is off by default
(`referenceDaycare.enabled`, see [configuration](configuration.md)) because it creates Pokémon for free; when off,
`deposit` and `breed` answer that it is disabled and change nothing, while `status` and `withdraw` keep working so
nothing is left stuck. Any player can use it:

| Command | Effect |
|---|---|
| `/daycare deposit <slot 1-6>` | Moves that party Pokémon into the daycare. Two places per player; refused during a battle or while that Pokémon is out of its Poké Ball. |
| `/daycare breed` | Breeds the two deposited Pokémon, if they are compatible. |
| `/daycare status` | Parents with their breedings and next loss chance, and the offspring waiting. |
| `/daycare withdraw` | Returns parents and offspring to the party, or to the PC when the party is full. What fits nowhere stays. Refused during a battle. |

Compatibility follows Cobbreeding: no parent in the `UNDISCOVERED` egg group or with `neutered = true` in its
persistent data; Ditto breeds with anything but another Ditto; a genderless Pokémon only with Ditto; otherwise a
male and a female that share an egg group. Each refusal has its own translated message. The offspring is the root
of the mother's line (or of the non-Ditto parent's), at level 1, with nothing inherited. No eggs, timers or NPCs.

`breed` follows the contract above in one server-thread call: it creates the offspring, calls
`LifecycleService.evaluate(a, b, "daycare", player)`, removes the lost parents and stores the offspring, all in the
daycare's own `SavedData` (`data/breedinglifecycle_daycare.dat` in the overworld), and marks it dirty once at the
end. The party and the PC are not touched, so a crash leaves the breeding either entirely done or not done at all
([0006](decisions/0006-breeding-atomicity.md), [0009](decisions/0009-reference-daycare.md)). The owner argument puts
the player in the audit lines, since a Pokémon held in the daycare has no store and so no owner of its own.
`deposit` and `withdraw` are transfers between a Cobblemon store and that `SavedData`: a crash between their two
saves can duplicate or drop that one Pokémon.

## Audit log rotation
`logs/breeding-lifecycle/audit.jsonl` only grows: the mod never rotates it, on purpose, and rotating it is the
integrator's job (logrotate or an equivalent). The mod opens the file for each line it writes, so a rotation that
moves the file away is safe; the next line starts a new file. An example for logrotate:

```
/path/to/server/logs/breeding-lifecycle/audit.jsonl {
    weekly
    rotate 8
    compress
    missingok
    notifempty
}
```
