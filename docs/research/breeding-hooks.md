# Breeding hooks for Cobblemon 1.7.3+1.21.1 (Fabric)

Research date: 2026-10-21. Scope: can a third-party mod find out when an egg is produced, and which
parents produced it, so it can evaluate parent loss? Every claim below links its source. Where the
source is the local jar, the evidence is a read-only `unzip -l` / byte grep of
`mods/Cobblemon-fabric-1.7.3+1.21.1.jar` of a Cobblemon 1.7.3+1.21.1 modpack instance (the instance was not modified).

## 1. Cobblemon 1.7.3 itself

**No breeding, egg items or daycare in core.** The jar contains no breeding, daycare or egg *item*
classes. Grepping class names for `egg|breed|hatch|daycare` returns only:

- `api/events/pokemon/CollectEggEvent`, `HatchEggEvent`, `HatchEggEvent$Pre`, `HatchEggEvent$Post` (event types)
- `api/pokemon/egg/EggGroup` and `util/adapters/EggGroupAdapter` (species egg-group data only)
- Exeggcute/Exeggutor models, `lucky_egg` and `ponigiri_egg` assets, and the `bull_breed` species feature (not related to breeding)

Both breeding mods describe themselves as adding breeding that core lacks: Cobbreeding is "a side-mod
for Cobblemon to add Pokémon breeding early"
([Modrinth](https://modrinth.com/mod/cobbreeding)), and Daycare+ is "a highly customisable and
advanced Cobblemon breeding experience" ([Modrinth](https://modrinth.com/mod/daycareplus)).

**The `CobblemonEvents` egg fields at tag `1.7.3`**
([CobblemonEvents.kt @ 1.7.3](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt)):

| Field | Type |
|---|---|
| `COLLECT_EGG` | `CancelableObservable<CollectEggEvent>` |
| `HATCH_EGG_PRE` | `CancelableObservable<HatchEggEvent.Pre>` |
| `HATCH_EGG_POST` | `EventObservable<HatchEggEvent.Post>` |

None of the fields mentions breeding or daycare. The compiled `CobblemonEvents.class` in the local jar
contains these same three names and no others.

Payloads:

- `CollectEggEvent(egg: PokemonProperties, maleParent: Pokemon, femaleParent: Pokemon, player: ServerPlayer) : Cancelable`
  ([CollectEggEvent.kt @ 1.7.3](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/pokemon/CollectEggEvent.kt)).
- `HatchEggEvent.Pre(egg: PokemonProperties, player)` and `HatchEggEvent.Post(player, pokemon: Pokemon)`.
  The jar's class strings show the same fields: `getEgg`/`setEgg` on Pre, and `getPokemon` on Post.

**Cobblemon defines these events but never fires them.** In the 1.7.3 jar, the only non-API classes
that reference `CollectEggEvent` or `HatchEggEvent` are listeners: `events/AdvancementHandler`,
`events/StatHandler` and `events/CallbackHandler`. Nothing in core emits them. They exist for side
mods to fire.

## 2. Breeding mods for Cobblemon on 1.21.1 Fabric

Version data comes from the Modrinth API
(`/v2/project/<slug>/version?game_versions=["1.21.1"]&loaders=["fabric"]`). Neither mod is installed
in that modpack instance; the instance's `mods/` folder has no breeding jar.

### 2.1 Cobbreeding (ludichat)

- Links: [Modrinth](https://modrinth.com/mod/cobbreeding) and [source](https://gitlab.com/ludichat/Cobbreeding).
- License: `LicenseRef-MIT-no-AI-no-reupload` ([license text](https://fuzuki.dev/~fuzuki/mit-no-ai-no-reupload.txt)).
- Latest build for Cobblemon 1.7: **2.2.2**, "Cobbreeding 2.2.2 [Cobblemon 1.7]", released 2026-07-06. Later builds target newer Cobblemon: 2.3.x needs 1.8 and 2.4.1 needs 1.8.2+ ([versions](https://modrinth.com/mod/cobbreeding/versions)).
- Sides: client required and server required ([Modrinth](https://modrinth.com/mod/cobbreeding)). It is **not** server-side only.
- How eggs are made: a mixin on Cobblemon's pasture ticker
  ([PokemonPastureBlockEntityMixin.java](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/java/ludichat/cobbreeding/mixin/PokemonPastureBlockEntityMixin.java#L97-173))
  calls `BreedingUtilities.chooseEgg(...)`, which picks the parent pair
  ([BreedingUtilities.kt](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/BreedingUtilities.kt#L118-160)).
- Events: the mod has **no events of its own**. It fires Cobblemon's events:
  - `COLLECT_EGG` fires when a player *takes* an egg out of the pasture, not when the egg is produced. It passes **placeholder parents** (`new Pokemon()` for both)
    ([PastureBlockMixin.java#L75-84](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/java/ludichat/cobbreeding/mixin/PastureBlockMixin.java#L75-84)).
    The same code is on `master`, and the released 2.2.2 jar references `CollectEggEvent` only from `PastureBlockMixin`.
  - `HATCH_EGG_PRE` and `HATCH_EGG_POST` fire on hatch
    ([PokemonEgg.kt#L214-239](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/PokemonEgg.kt#L214-239)).
- Verdict: no hook exposes the real parents. Getting them would need a **mixin into Cobbreeding's own
  internals**, such as `BreedingUtilities.chooseEgg` or its own pasture mixin. That is brittle and
  breaks between versions.
- Maven: the build is available as `maven.modrinth:cobbreeding:2.2.2` (the POM returned HTTP 200 from `https://api.modrinth.com/maven`).

### 2.2 Daycare+ (Provismet)

- Links: [Modrinth](https://modrinth.com/mod/daycareplus), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/daycareplus), [source](https://github.com/Provismet/DaycarePlus) and [wiki](https://github.com/Provismet/DaycarePlus/wiki).
- License: Lily License - Server v1.0, a custom license. It allows use and inclusion in modpacks, but **the mod may not be redistributed** and commercial use needs a separate license ([LICENSE](https://github.com/Provismet/DaycarePlus/blob/master/LICENSE)).
- Latest build for Cobblemon 1.7: **1.4.2+1.21.1**, released 2026-08-16. The source at that tag pins
  `cobblemon_version=1.7.3+1.21.1`
  ([gradle.properties @ 1.4.2-mc1.21.1](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/gradle.properties)).
  Version 1.5.0 moved to Cobblemon 1.8.0, and 1.5.1 (2026-10-17) moved to 1.8.2 ([versions](https://modrinth.com/mod/daycareplus/versions)).
- Sides: client optional, server required. It works server-side only: "players without the mod can
  join" ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/daycareplus)).
- Public API: the
  [`DaycarePlusEvents`](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/api/DaycarePlusEvents.java)
  interface defines three Fabric `Event`s:
  - `PRE_EGG_PRODUCED`: `(PokemonProperties)`. It carries **no parents**.
  - `POST_EGG_PRODUCED`: `(ItemStack)`. It carries no parents and also fires when egg data is turned back into an item.
  - `EGG_PROPERTIES_CREATED`: `(Pokemon primary, Pokemon secondary, PokemonProperties)`. It does carry the parents, but it fires inside `createPokemonProperties()`
    ([PotentialPokemonProperties.java#L95](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/breeding/PotentialPokemonProperties.java#L95)).
    That method is also called by the **GUI preview**
    ([DaycareGUI.java#L122](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/gui/DaycareGUI.java#L122)),
    so this event does not reliably mean that an egg was produced.
- Cobblemon event: `PastureExtension.produceEgg(...)` fires
  `CobblemonEvents.COLLECT_EGG.emit(new CollectEggEvent(properties, potentialEgg.getPrimary(), potentialEgg.getSecondary(), serverPlayer))`
  **at production time, with the real tethered parents**
  ([PastureExtension.java#L112-156](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/breeding/PastureExtension.java#L112-156), emit at L148).
  This holds for normal ticks, offline catch-up and the Daycare Spark item
  ([PastureExtension.java#L183-207](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/breeding/PastureExtension.java#L183-207),
  [DaycareSparkItem.java#L40](https://github.com/Provismet/DaycarePlus/blob/1.4.2-mc1.21.1/src/main/java/com/provismet/cobblemon/daycareplus/item/DaycareSparkItem.java#L40)).
  Caveats:
  - The event fires **only if the pasture owner is online**, because `owner` comes from `getPlayerByUuid` and the emit sits inside `if (owner instanceof ServerPlayerEntity)` (L118, L147-149).
  - The call site ignores the result of `emit`, so cancelling the event does not stop the egg.
  - In "competitive breeding" mode, Daycare+ already decrements a fertility counter on both parents (L127-129).
- Maven: `maven.modrinth:daycareplus:1.4.2+1.21.1` (the POM returned HTTP 200 from `https://api.modrinth.com/maven`). Using `COLLECT_EGG` does not need it.

### 2.3 Others found

- [Very Scuffed Cobblemon Breeding](https://modrinth.com/mod/veryscuffedcobblemonbreeding): MIT, 1.1.1+1.21.1 (2025-02-10), server-side. It breeds instantly through a `/pokebreed` command and GUI ([PokeBreed.java](https://github.com/ThomasQTruong/VeryScuffedCobblemonBreeding/blob/004754195af6ef7739165ac87509220bab3fbf9f/common/src/main/java/dev/thomasqtruong/veryscuffedcobblemonbreeding/commands/PokeBreed.java)). Its source never references `CobblemonEvents` and it has no API. Hooking it would need a mixin.
- Search found no Fabric 1.21.1 Cobblemon mods named "Cobbled Daycare" or "PokeBreeder" ([Modrinth search](https://modrinth.com/mods?q=cobblemon%20breeding&g=1.21.1)).
- The other search hits are add-ons for Cobbreeding, not breeding systems: Cobblemon Incubator, Hatchwatch, IV Candy Breeding and Pasture Enhanced ([Modrinth search](https://modrinth.com/mods?q=cobblemon%20egg&g=1.21.1)).

## 3. Conclusion

**A real hook exists, and it is Cobblemon's own event. No breeding mod needs to be a dependency.**

- Hook: subscribe to `com.cobblemon.mod.common.api.events.CobblemonEvents.COLLECT_EGG`. The listener
  gets a `CollectEggEvent` with `egg: PokemonProperties`, `maleParent: Pokemon`, `femaleParent: Pokemon` and `player: ServerPlayer`.
- Dependency: compile only against Cobblemon `1.7.3+1.21.1`, which the mod already depends on. **No
  mixin is needed, and there is no compile or runtime dependency on any breeding mod.** If neither mod
  is installed, the listener never fires.
- Which mods give usable parents:
  - **Daycare+ 1.4.2: yes.** It fires the event when the egg is produced, with the real parent
    `Pokemon` objects, but only while the owner is online.
  - **Cobbreeding 2.2.2: no.** It fires the event when the egg is collected, with blank
    `new Pokemon()` placeholders. The adapter must detect these and skip them. One way: the
    placeholder's UUID is in no player's party or PC, or it is not tethered to any pasture. Real parent
    data for Cobbreeding would need a mixin into Cobbreeding's internals, which this research advises
    against.
- Do not use Daycare+'s `EGG_PROPERTIES_CREATED`: it also fires on GUI previews.
- Do not rely on cancelling `COLLECT_EGG`: neither mod checks the result.
- The adapter should therefore be **optional and best-effort**: it reacts to `COLLECT_EGG` with real
  parents, and in practice that means Daycare+ only. Cobbreeding parent-loss support, and Daycare+ eggs
  produced while the owner is offline, are out of reach without mixins.
