# Writing a Cobblemon 1.7.3 addon in Java 21 (Fabric, MC 1.21.1)

Research date: 2026-10-21. Target: `Cobblemon-fabric-1.7.3+1.21.1.jar`. Cobblemon is written in Kotlin; this note covers what a Java 21 server-side addon can call and how.

**Evidence sources**
- Source: the `1.7.3` tag of [gitlab.com/cable-mc/cobblemon](https://gitlab.com/cable-mc/cobblemon/-/tree/1.7.3) (commit `e6fda6137e`). Links below are abbreviated as `C:<path>#L<n>`, which stands for `https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/<path>#L<n>`.
- Bytecode: I read the installed jar without changing it. No JDK is installed, so instead of `javap` I used `unzip -l` plus a small constant-pool parser. "JVM sig" below means the access flags and descriptors read from those `.class` files. That is what Java code actually links against.
- Cobbreeding (the main breeding addon), branch `cobblemon-1.7` of [gitlab.com/ludichat/Cobbreeding](https://gitlab.com/ludichat/Cobbreeding/-/tree/cobblemon-1.7), abbreviated `CB:<path>`.
- The official Java event snippet: [cable-mc/cobblemon snippet 4881740](https://gitlab.com/cable-mc/cobblemon/-/snippets/4881740).

## 1. Dependency setup

- The Maven artifact `com.cobblemon:fabric:1.7.3+1.21.1` is published at [maven.impactdev.net/repository/development](https://maven.impactdev.net/repository/development/com/cobblemon/fabric/maven-metadata.xml).
- The jar's `fabric.mod.json` depends on `fabricloader >=0.17.2`, `fabric-api >=0.116.6+1.21.1`, `minecraft 1.21.1` and `java 21`. It nests `fabric-language-kotlin-1.13.6+kotlin.2.2.20` (from the jar; the source is linked in its `contact.sources`, [gitlab.com/cable-mc/cobblemon](https://gitlab.com/cable-mc/cobblemon)).
  - So at runtime the Kotlin stdlib (`kotlin.Unit`, `kotlin.jvm.functions.Function1`) is present.
  - At compile time a Java addon needs `org.jetbrains.kotlin:kotlin-stdlib` as `compileOnly` only if it names Kotlin types such as `Unit`.
  - The instance also ships `fabric-language-kotlin-1.13.11+kotlin.2.3.21.jar` on its own (from the instance `mods/` listing).
- Classes in the jar use intermediary names (`net/minecraft/class_3222` = `ServerPlayer`, `class_2487` = `CompoundTag`). Loom remaps them for you. Do not hard-code them.

## 2. Public API surface relevant to breeding

### Entry points (Kotlin `object`s → Java `INSTANCE`)
| Kotlin | Java call | Evidence |
|---|---|---|
| `Cobblemon.storage` | `Cobblemon.INSTANCE.getStorage()` (not static) | JVM sig: `INSTANCE` is `public static final`, `getStorage()` is an instance method |
| `PokemonSpecies.getByName("eevee")` | `PokemonSpecies.getByName(...)` works statically; `PokemonSpecies.INSTANCE.getByName(...)` also works | JVM sig: `getByName`, `getByIdentifier`, `getSpecies`, `random` have flags `0x19` (static, so `@JvmStatic`) |
| `PokemonProperties.parse(s)` | `PokemonProperties.Companion.parse("species=eevee level=1")` | JVM sig: `parse(String)`, `parse(String,String)` and `parse(String,String,String)` exist only on `PokemonProperties$Companion`, not as statics |
| `CobblemonEvents.X` | `CobblemonEvents.X` is a static field | [C:api/events/CobblemonEvents.kt#L54](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt#L54): every field is `@JvmField` |

### Species and creation
- `Species.eggGroups` is a `HashSet<EggGroup>` ([C:pokemon/Species.kt#L108](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Species.kt#L108)).
  - Forms carry their own groups as well (`FormData.eggGroups`), and Cobbreeding uses `pokemon.form.eggGroups`, not the species' ([CB:BreedingUtilities.kt#L175](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/BreedingUtilities.kt#L175)).
- `Species.maleRatio` is a `Float`: `-1` means genderless, `0` means always female, `1` means always male ([Species.kt#L71](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Species.kt#L71), [#L141-L145](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Species.kt#L141)).
- `Species.create(level: Int = 10)` is implemented as `PokemonProperties.parse("species=\"<id>\" level=<n>").create()` ([Species.kt#L206](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Species.kt#L206)).
  - **The default level is 10, not 1.** From Java you must pass the level: `species.create(1)`.
  - It has no `@JvmOverloads`. JVM sig shows only `create(I)` and the synthetic `create$default`.
- Use `PokemonProperties.Companion.parse("species=cobblemon:eevee level=1 form=...").create()` when you need form or aspect control. `create()` and `create(ServerPlayer)` both exist (JVM sig).
- `EggGroup` is a Kotlin enum. It has 15 values, including `DITTO` and `UNDISCOVERED` ([C:api/pokemon/egg/EggGroup.kt#L32-L34](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/pokemon/egg/EggGroup.kt#L32)). Java uses it as a normal enum: `EggGroup.UNDISCOVERED`.
- `Gender` is an enum with `MALE`, `FEMALE` and `GENDERLESS` (JVM sig of `com/cobblemon/mod/common/pokemon/Gender`). Read it with `pokemon.getGender()`.

### Breeding compatibility in Cobblemon 1.7.3: none
- Base Cobblemon 1.7.3 has **no breeding, egg item or daycare**.
  - The jar contains no egg item or breeding logic, only `EggGroup`, `EggGroupAdapter` and the egg events (from `unzip -l` on the jar).
  - The [Pasture Block wiki page](https://wiki.cobblemon.com/index.php/Pasture_Block) describes the pasture only as letting out up to 16 Pokémon, with no mention of eggs.
- Cobblemon declares the egg events but **never emits them itself**:
  - `COLLECT_EGG` is a `CancelableObservable<CollectEggEvent>` ([CobblemonEvents.kt#L349](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt#L349)). `HATCH_EGG_PRE` and `HATCH_EGG_POST` follow it ([#L352](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt#L352)).
  - In the jar, the only references to `COLLECT_EGG` are subscribers in `AdvancementHandler`, `StatHandler` and `CallbackHandler`.
  - Addons emit them. Cobbreeding fires `COLLECT_EGG` from its pasture mixin ([CB:mixin/PastureBlockMixin.java#L75-L79](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/java/ludichat/cobbreeding/mixin/PastureBlockMixin.java#L75)) and the hatch events from its egg item ([CB:PokemonEgg.kt#L214](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/PokemonEgg.kt#L214), [#L239](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/PokemonEgg.kt#L239)).
- `CollectEggEvent(egg: PokemonProperties, maleParent: Pokemon, femaleParent: Pokemon, player: ServerPlayer)` is `Cancelable` ([C:api/events/pokemon/CollectEggEvent.kt](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/pokemon/CollectEggEvent.kt)). It is the cleanest hook for "a breeding just happened, with both parents known".
- The de facto compatibility rules are Cobbreeding's `getPossibleEggs` ([CB:BreedingUtilities.kt#L170-L238](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/BreedingUtilities.kt#L170)):
  1. Skip a Pokémon whose `neutered` flag is true.
  2. Skip the pair if either form is in `UNDISCOVERED`.
  3. If one side is `DITTO`, the non-Ditto side determines the baby, so `GENDERLESS` + Ditto works. Ditto + Ditto is handled separately ([#L137](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/BreedingUtilities.kt#L137)).
  4. Otherwise the pair needs a shared egg group **and** one `FEMALE` plus one `MALE`. The baby comes from the female's form, and genderless non-Ditto pairs never match.
  5. Nidoran♀/♂ and Volbeat/Illumise are cross-added.

## 3. Neutered / castrated flag
- **Cobblemon 1.7.3 has nothing.** A search for `neuter`, `sterile` or `breedable` across all ~15k Cobblemon classes in the jar finds nothing. There is no field or property for it on `Pokemon`.
- **Cobbreeding has one** (branch `cobblemon-1.7`, which also has a `feature/neuter` branch):
  - The flag is `NeuterProperty`, a `CustomPokemonPropertyType<BooleanProperty>` with the key `"neutered"`. It is stored as `pokemon.persistentData.putBoolean("neutered", v)` followed by `pokemon.onChange()` ([CB:NeuterProperty.kt](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/NeuterProperty.kt)).
  - The key is not namespaced, sits at the root of `persistentData`, and an absent key means "not neutered".
  - Because it is registered as a custom property, `neutered=true` also works in `PokemonProperties` strings.
- **Cobbreeding's flag is reversible.**
  - The original trainer (OT) can toggle it from the summary GUI. The server checks `player.stringUUID == pokemon.originalTrainer` ([CB:network/ToggleNeuterPacket.kt](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/network/ToggleNeuterPacket.kt)).
  - A third-party addon, "disable cobbreeding breeding", changes that toggle into a permanent one-way action ([moddex listing](https://moddex.gg/mod/disable-cobbreeding-breeding-addon-mr-hhi1jMm5)). It is unverified beyond that listing.
- Cobbreeding is **not installed** in the Cobblemon 1.7.3+1.21.1 modpack instance used for the research: no matching jar in its `mods/` folder (`ls` of the instance).

## 4. `Pokemon.persistentData`
- The declaration is `var persistentData: CompoundTag = CompoundTag()` with an `internal set` ([C:pokemon/Pokemon.kt#L652-L658](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L652)).
  - In Java, `getPersistentData()` is public. The setter is the mangled `setPersistentData$common`, which is effectively private. **Mutate the tag in place and never replace it.**
- Its KDoc warns that changing it does not mark the Pokémon dirty: **call `onChange()` or you may see reversions** ([Pokemon.kt#L652-L655](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L652)).
  - `onChange(packet: PokemonUpdatePacket<*>? = null)` has no `@JvmOverloads`, so Java calls `pokemon.onChange(null)` (JVM sig).
  - It is a no-op when the Pokémon is in no store ([#L2134-L2141](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L2134)), which covers wild Pokémon and Pokémon not yet added to a store.
- **It is serialized** under the `DataKeys.POKEMON_PERSISTENT_DATA` key (the string `"PersistentData"` in `DataKeys.class`) by both the save codec (`PokemonP2`) and the client codec ([C:util/codec/internal/ClientPokemonP2.kt#L88](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/util/codec/internal/ClientPokemonP2.kt#L88)).
  - So it **survives save/load in party and PC** and is **synced to clients**. Treat it as visible to players and never put secrets there.
- **It travels with the Pokémon:**
  - In a trade, Cobblemon moves the same `Pokemon` object: `party1.remove(pokemon1)` … `party2.add(pokemon1)` ([C:trade/TradeManager.kt#L102-L123](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/trade/TradeManager.kt#L102)).
  - `clone()` round-trips through the codec and so keeps it, but assigns a **new UUID** by default (`newUUID = true`) ([Pokemon.kt#L1356-L1368](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L1356)).
  - `copyFrom` shares the tag reference ([#L1418](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L1418)).
- **Namespacing.** Cobblemon does not enforce any namespacing, and Cobbreeding already uses the bare `neutered` key. Put this addon's data in one nested compound, e.g. `persistentData.getCompound("breeding_lifecycle")`. Read Cobbreeding's root `neutered` key read-only if you want interop.

## 5. Party / PC storage
- `PokemonStoreManager` offers `getParty(ServerPlayer)`, `getParty(UUID, RegistryAccess)`, `getPC(ServerPlayer)`, `getPC(UUID, RegistryAccess)` and `getPCs(UUID, RegistryAccess)` ([C:api/storage/PokemonStoreManager.kt#L49-L79](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/PokemonStoreManager.kt#L49)).
- `PlayerPartyStore.add(pokemon)` does three things ([C:api/storage/party/PlayerPartyStore.kt#L61](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/party/PlayerPartyStore.kt#L61)):
  - It sets the original trainer if the Pokémon has none.
  - If the party is full, it **overflows to the PC**.
  - It returns `false` and messages the player if both are full.
- `PartyStore.remove(pokemon)` returns a boolean and sends `RemoveClientPokemonPacket` to observers ([C:api/storage/party/PartyStore.kt#L106](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/party/PartyStore.kt#L106)).
- Other party methods (JVM sig): `get(int)`, `get(PartyPosition)`, `size()`, `iterator()`, `toGappyList()`, `getPlayerUUID()`. `PCStore` has `add`, `remove` and `get(PCPosition)`.
- To look a Pokémon up by its UUID, Cobbreeding does `Cobblemon.storage.getParty(player)[uuid]` (Kotlin `get(UUID)`) in [ToggleNeuterPacket.kt](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/network/ToggleNeuterPacket.kt).

## 6. `CobblemonEvents` from Java
- Every event field is `@JvmField` on an `object` ([CobblemonEvents.kt#L54-L60](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt#L54)). In Java, write `CobblemonEvents.POKEMON_CAPTURED` directly; there are no `getX()` and no `INSTANCE` hop.
- `Observable` declares three `subscribe` overloads ([C:api/reactive/Observable.kt#L33-L35](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/reactive/Observable.kt#L33)):
  - `subscribe(priority = NORMAL, handler: (T) -> Unit)`: a `Function1`, so Java must `return Unit.INSTANCE;`.
  - `subscribe(Consumer<T>)`: Java-friendly, no `Unit` needed.
  - `subscribe(Priority, Consumer<T>)`: Java-friendly, no `Unit` needed.
- The official Java snippet uses the `Function1` form with `return Unit.INSTANCE;` ([snippet 4881740](https://gitlab.com/cable-mc/cobblemon/-/snippets/4881740)).
- **Prefer the `Consumer` overloads.**
  - A block lambda with a `void` body binds only to `Consumer`, so it is unambiguous.
  - An *expression* lambda whose body returns a value (e.g. `e -> list.add(e)`) can match both overloads and fail to compile as ambiguous. Use a block body.
- `Priority` is an enum with `HIGHEST`, `HIGH`, `NORMAL`, `LOW` and `LOWEST` (JVM sig).
- `subscribe` returns `ObservableSubscription`. Keep it if you need to `unsubscribe`.
- Cancelable events (`CancelableObservable`, e.g. `COLLECT_EGG`, `POKEMON_RELEASED_EVENT_PRE` at [#L222](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/events/CobblemonEvents.kt#L222), `TRADE_EVENT_PRE`) expose `event.cancel()`.

```java
CobblemonEvents.COLLECT_EGG.subscribe(Priority.NORMAL, event -> {
    Pokemon mother = event.getFemaleParent();          // Kotlin val -> getter
    if (BreedingFlags.isNeutered(mother)) event.cancel();
});
```

**General Kotlin-from-Java rules** (confirmed in the bytecode above):
- A `val x` is read with `getX()`.
- A Kotlin `object` is reached through `X.INSTANCE` unless its members are `@JvmStatic` or `@JvmField`.
- A `companion object` member is reached as `X.Companion.m()` unless it is `@JvmStatic`.
- **Default arguments do not exist in Java** unless the function has `@JvmOverloads`, so pass every argument: `species.create(1)`, `pokemon.onChange(null)`, `pokemon.saveToNBT(registryAccess, new CompoundTag())`. Never call the synthetic `*$default` methods.
- Kotlin `T?` return types (e.g. `getOwnerUUID()`, `getOwnerPlayer()`, `PokemonSpecies.getByName()`) carry no compile-time nullness in Java, so null-check every one of them.
- `internal` members are compiled with mangled names (`isClient$common`, `setPersistentData$common`). Do not call them.

## 7. Identity and owner for audit logging
- **Pokémon identity**
  - `pokemon.getUuid()` returns the UUID, generated randomly at construction ([Pokemon.kt#L175](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L175)). It is stable across save, party↔PC and trade, but **not across `clone()`**.
- **Current owner**
  - `getOwnerUUID()` reads it from `storeCoordinates` ([#L1467-L1477](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L1467)):
    - for a party, it is the player UUID;
    - for a PC, it is the PC store UUID, which the code returns as `it.store.uuid`, so check that it equals the player UUID in your setup;
    - for an NPC party, it is the NPC UUID;
    - otherwise it is `null`.
  - `getOwnerPlayer()` returns the `ServerPlayer` only if they are online ([#L1459](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L1459)).
  - `isPlayerOwned()` and `isWild()` exist ([#L1480](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L1480)).
- **Original trainer**
  - `getOriginalTrainer()` returns a `String?`. For players it is the UUID string ([#L506](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/pokemon/Pokemon.kt#L506); Cobbreeding compares it to `player.stringUUID`).
- **What to log:** the Pokémon UUID, species identifier (`getSpecies().getResourceIdentifier()`), owner UUID, OT and the acting player UUID. Take these from the event's `player` where one is given, rather than inferring them from the store.

## 8. What to avoid
- **Mixins.**
  - Every hook this project needs is reachable without a mixin: events for capture, trade, release and egg collect/hatch, store APIs, and `persistentData`.
  - A mixin into Cobblemon's Kotlin classes is brittle across patch releases. Cobbreeding needs five mixins only because it adds GUI and pasture behaviour ([CB tree](https://gitlab.com/ludichat/Cobbreeding/-/tree/cobblemon-1.7/common/src/main/java/ludichat/cobbreeding/mixin)).
- **Off-thread access.**
  - `PartyStore.remove`/`set` send packets to observers and `onChange` notifies the store (§4, §5), so the store and network code assume the server thread.
  - Do database or audit I/O asynchronously if you like, but mutate Pokémon and stores only on the server thread (`server.execute(...)`).
- **Client classes in a server mod.**
  - Cobblemon ships client code under `com.cobblemon.mod.common.client.*`, loaded through a separate `client` entrypoint (the jar's `fabric.mod.json`).
  - Cobbreeding's own neuter packet handler touches `CobblemonClient` ([ToggleNeuterPacket.kt](https://gitlab.com/ludichat/Cobbreeding/-/blob/cobblemon-1.7/common/src/main/kotlin/ludichat/cobbreeding/network/ToggleNeuterPacket.kt)).
  - A server-only addon must not import `client` packages, `Minecraft` or `CobblemonClient`.
- **Replacing `persistentData` or forgetting `onChange(null)`.** See §4.
- **Relying on `Species.create()`'s default level** (10). See §2.

## Recommendations for this project
1. **Build setup**
   - Use `modImplementation "com.cobblemon:fabric:1.7.3+1.21.1"` from the impactdev Maven, plus `fabric-language-kotlin` as a runtime dependency.
   - Add `kotlin-stdlib` as `compileOnly` only if you reference `Unit` or `Function1`.
   - Declare `"depends": {"cobblemon": ">=1.7.3"}`. Keep it server-side only, with a `main` entrypoint and no `client` one.
2. **Subscribe in Java** with `subscribe(Priority, Consumer)` and block lambdas. Never return `Unit` unless you need the `Function1` overload.
3. **Store addon state** in one namespaced compound: `persistentData.getCompound("breeding_lifecycle")` (`breeding_lifecycle` is the namespace this note proposes for the project). Then put it back and call `pokemon.onChange(null)`. Assume clients can read it.
4. **Neutering**
   - Implement it as your own permanent flag inside that compound.
   - For interop, also **read** Cobbreeding's root `neutered` boolean, and optionally **write** `neutered=true` so Cobbreeding skips the Pokémon too. Remember that Cobbreeding lets the OT toggle that key back.
   - As a safety net, cancel `COLLECT_EGG` when either parent is neutered.
5. **Compatibility checks**
   - Mirror Cobbreeding's rules exactly: form egg groups; `UNDISCOVERED` never breeds; `DITTO` pairs with anything non-Undiscovered; otherwise a shared egg group plus male×female; `GENDERLESS` only with Ditto.
   - Don't invent new rules. Base Cobblemon defines none.
6. **Creating offspring:** `PokemonProperties.Companion.parse("species=<id> level=1").create()`, or `species.create(1)`. Add it with `getParty(player).add(p)` and handle a `false` return, which means both party and PC are full.
7. **Audit logging**
   - Log `getUuid()`, the species identifier, `getOwnerUUID()`, `getOriginalTrainer()`, the acting player, and the event name.
   - Write the log off-thread, but capture the values on the server thread first.
   - Note that the UUID changes on `clone()`.
8. **No mixins.** Use the `COLLECT_EGG` and `HATCH_EGG_*` events, which only fire if a breeding addon such as Cobbreeding emits them, plus the `TRADE_EVENT_*`, `POKEMON_RELEASED_EVENT_*` and `POKEMON_CAPTURED` events.
9. **Clarify dependency on Cobbreeding.** The target instance does not include Cobbreeding. If this addon is meant to be the breeding mechanism, it must implement egg production itself, because base 1.7.3 has none. Confirm this with the project owner before planning.
