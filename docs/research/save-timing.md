# When Cobblemon stores and `SavedData` are written (Cobblemon 1.7.3, MC 1.21.1)

Research date: 2026-10-21. Question from S8: is a breeding that runs inside one server-thread task atomic
against a crash, and what does it take for that to hold?

Sources: Cobblemon tag `1.7.3` on GitLab (`CB:` = `https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/`), Fabric API
branch `1.21.1` on GitHub, and Minecraft 1.21.1 classes in Mojang names, read with `javap -c` from the jar Loom
builds for `mod` (`.gradle/loom-cache/minecraftMaven/.../minecraft-merged-*.jar`); bytecode offsets are given as `@n`.

## Cobblemon party and PC stores
- The default factory is `FileBackedPokemonStoreFactory`, registered in `Cobblemon.kt`
  ([CB:Cobblemon.kt#L400-L407](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/Cobblemon.kt#L400)).
- Changes mark a store dirty: `track` subscribes to the store's `getAnyChangeObservable()` with
  `subscribeOnServer { dirtyStores.add(store) }`
  ([CB:FileBackedPokemonStoreFactory.kt#L117-L120](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L117)).
  `Pokemon.onChange` feeds that observable, which is why the counter write calls `onChange(null)`.
- Periodic save: a `SERVER_TICK_PRE` subscriber counts ticks and, every `20 * pokemonSaveIntervalSeconds` ticks,
  calls `saveAll`
  ([#L42-L48](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L42)).
  The interval defaults to 30 s, range 1-120
  ([CB:CobblemonConfig.kt#L68-L70](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/config/CobblemonConfig.kt#L68)).
- `saveAll` **serializes every dirty store on the calling thread** (the server thread, inside the tick event),
  clears the dirty set, and only then hands the already-serialized snapshots to a single-thread executor that
  writes the files ([#L103-L112](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L103)).
  `save(store)` does the same for one store ([#L97-L101](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L97)).
- Other saves: on player disconnect, that player's dirty stores
  ([#L129-L132](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L129)),
  and on `SERVER_STOPPED` through `shutdown`, which calls `saveAll` and waits up to 30 s for the executor
  ([#L122-L127](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/factory/FileBackedPokemonStoreFactory.kt#L122),
  [CB:Cobblemon.kt#L410-L415](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/Cobblemon.kt#L410)).
- The file write goes to `<file>.temp` first, then copies over the real file
  ([CB:OneToOneFileStoreAdapter.kt#L57-L67](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/common/src/main/kotlin/com/cobblemon/mod/common/api/storage/adapter/flatfile/OneToOneFileStoreAdapter.kt#L57)).
- `SERVER_TICK_PRE` is Fabric's `START_SERVER_TICK`
  ([CB:CobblemonFabric.kt#L170-L171](https://gitlab.com/cable-mc/cobblemon/-/blob/1.7.3/fabric/src/main/kotlin/com/cobblemon/mod/fabric/CobblemonFabric.kt#L170)),
  which Fabric fires inside `MinecraftServer.tickServer` right before `tickChildren` (Yarn `tick`/`tickWorlds`)
  ([MinecraftServerMixin.java#L67-L75](https://github.com/FabricMC/fabric/blob/1.21.1/fabric-lifecycle-events-v1/src/main/java/net/fabricmc/fabric/mixin/event/lifecycle/MinecraftServerMixin.java#L67)).

## Minecraft `SavedData`
- `MinecraftServer.tickServer` runs `tickChildren` (@23, every level, connections and commands), then counts
  `ticksUntilAutosave` down and, at zero, calls `saveEverything(true, false, false)` (@54-@104), on the server thread.
- `saveEverything` calls `saveAllChunks` (@16), which calls `ServerLevel.save` per level (@82); that calls
  `saveLevelData` (@28), which calls `DimensionDataStorage.save()` (@33). It runs `SavedData.save(File, ...)` for each
  cached entry (`DimensionDataStorage.save` @5-@10).
- `SavedData.save(File, ...)` returns early unless `isDirty()`, builds the tag with `save(CompoundTag, ...)` and writes
  it **synchronously** with `NbtIo.writeCompressed` (@1-@45), then clears the dirty flag (@68).
- The server also saves on stop (`stopServer` → `saveAllChunks`, @224); a crash or `kill -9` skips both.

## Conclusion
1. Both persistence paths read game state **only on the server thread, between pieces of game logic**: Cobblemon
   snapshots at `START_SERVER_TICK`, Minecraft writes `SavedData` after `tickChildren`. A breeding that runs entirely
   in one server-thread task (one command execution, one event handler) cannot be split by either snapshot.
2. The two stores are written **at different moments**: Cobblemon every 30 s (and on disconnect), `SavedData` at
   the vanilla autosave (minutes) and on stop. A crash between them keeps one store's new state and the other's old one.
3. Therefore S8 holds with "one tick, one `SavedData`": a breeding that changes only the daycare's `SavedData`
   (parents, counters and offspring) is atomic. A breeding that also edits a Cobblemon store is not.
   See [0006](../decisions/0006-breeding-atomicity.md).
