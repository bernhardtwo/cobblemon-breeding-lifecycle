# Live server test

The mod run on a real dedicated Fabric server next to the mods of a Cobblemon 1.7.3+1.21.1 modpack (CurseForge,
pack 1.0.12), with no client. Run on 2026-10-21. What still needs a client is listed at the end.

## Setup
The server lives outside the repository and is not committed.
The CurseForge instance was only read: its mods were copied, nothing in it was written.

| Piece | Version |
|---|---|
| Minecraft | 1.21.1, dedicated server |
| Fabric Loader | 0.19.2 (server launcher from `meta.fabricmc.net`, installer 1.1.2) |
| Java | OpenJDK 21.0.13, `-Xmx4G` |
| Cobblemon | 1.7.3+1.21.1 |
| Fabric API | 0.116.11+1.21.1 |
| Fabric Language Kotlin | 1.13.11+kotlin.2.3.21 |
| Breeding Lifecycle | 0.1.0+1.21.1, `mod/build/libs/breeding-lifecycle-0.1.0+1.21.1.jar` |

Cobblemon, Fabric API and Kotlin in the pack are exactly the versions pinned in `gradle.properties`.
`server.properties`: `online-mode=true`, flat world, creative. One operator, the tester's account.

Scripts in `live-server/`:

| Script | Does |
|---|---|
| `select-mods.py` | Reads `fabric.mod.json` of each pack jar; copies those not declaring `"environment": "client"`; writes `mods.csv`. |
| `start.sh` | Starts the server with its console on a FIFO, turns the daycare on and gives the player a male and a female Charmander on every join. Ctrl+C stops it. |
| `stop.sh` | Sends `stop` to the console. |
| `loss.sh` | Writes `"overrides": { "p1": 1.0 }` and runs `breedinglifecycle reload`. |
| `reset.sh` | Writes the default curve with the daycare on and runs `breedinglifecycle reload`. |

## Mods
The pack has 90 jars: 53 load on the server, 37 are client-only, plus this mod. The server
loads 133 mods in all, counting nested libraries. No mod crashed the server, so none was removed for that reason.

### Loaded (54)
| File | Mod id | Version | Reason |
|---|---|---|---|
| `CobbleFurnies-fabric-1.1.jar` | `cobblefurnies` | 1.1 | environment * |
| `CobbledollarDummy-1.0.0.jar` | `cobbledollardummy` | 1.0.0 | environment * |
| `Cobblemon-fabric-1.7.3+1.21.1.jar` | `cobblemon` | 1.7.3+1.21.1 | environment * |
| `CobblemonSizeVariationFabric-1.4.0+1.7.0.jar` | `cobblemonsizevariation` | 1.4.0 | environment * |
| `CobblemonTrialsEdition-fabric-1.3.1.jar` | `cobblemontrialsedition` | 1.3.1 | environment * |
| `Cobblepedia-Fabric-0.7.1.jar` | `cobblepedia` | 0.7.1 | environment * |
| `CraftTweaker-fabric-1.21.1-21.0.38.jar` | `crafttweaker` | 21.0.38 | environment * |
| `CreativeCore_FABRIC_v2.13.37_mc1.21.1.jar` | `creativecore` | 2.13.37 | environment * |
| `ForgeConfigAPIPort-v21.1.6-1.21.1-Fabric.jar` | `forgeconfigapiport` | 21.1.6 | environment * |
| `Iceberg-1.21.1-fabric-1.3.2.jar` | `iceberg` | 1.3.2 | environment * |
| `Jade-1.21.1-Fabric-15.10.5.jar` | `jade` | 15.10.5+fabric | environment * |
| `JadeAddons-1.21.1-Fabric-6.0.3.jar` | `jadeaddons` | 6.0.3+fabric | environment * |
| `JeiTweaker-fabric-1.21.1-8.0.jar` | `jeitweaker` | 8.0 | environment * |
| `MoreCobblemonTweaks-fabric-1.3.3.jar` | `more_cobblemon_tweaks` | 1.3.3 | environment * |
| `MossyLib-1.1.0+1.21.1+fabric.jar` | `mossylib` | 1.1.0+1.21.1+fabric | environment * |
| `NoChatReports-FABRIC-1.21.1-v2.9.1.jar` | `nochatreports` | 1.21.1-v2.9.1 | environment * |
| `OctoLib-FABRIC-0.6.1+1.21.jar` | `octolib` | 0.6.1 | environment * |
| `Patchouli-1.21.1-93-FABRIC.jar` | `patchouli` | 1.21.1-93-FABRIC | environment * |
| `SimpleTMs-fabric-2.3.3.jar` | `simpletms` | 2.3.3 | environment * |
| `SubtleEffects-fabric-1.21.1-1.14.3.jar` | `subtle_effects` | 1.14.3 | environment * |
| `accessories-fabric-1.1.0-beta.53+1.21.1.jar` | `accessories` | 1.1.0-beta.53+1.21.1 | environment * |
| `architectury-13.0.8-fabric.jar` | `architectury` | 13.0.8 | environment * |
| `athena-fabric-1.21-4.0.2.jar` | `athena` | 4.0.2 | environment * |
| `awcapi-fabric-1.21.1-1.0.2.jar` | `awcapi` | 1.0.2 | environment * |
| `cloth-config-15.0.140-fabric.jar` | `cloth-config` | 15.0.140 | environment * |
| `cobblemon-additions-4.3.0.jar` | `cobblemon-additions` | 4.3.0 | environment * |
| `cobblemon-battle-extras-fabric-1.13.45.jar` | `cobblemon-battle-extras` | 1.13.45 | environment * |
| `cobblemonchallenge-fabric-2.4.0.jar` | `cobblemonchallenge` | 2.4.0 | environment * |
| `cobblenav-fabric-2.3.3.jar` | `cobblenav` | 2.3.3 | environment * |
| `dynamic-lighting-1.0.jar` | `mr_dynamic_lighting` | 1.0 | environment * |
| `emotecraft-for-MC1.21.1-2.4.12-fabric.jar` | `emotecraft` | 2.4.12 | environment * |
| `entityculling-fabric-1.10.1-mc1.21.1.jar` | `entityculling` | 1.10.1 | environment * |
| `fabric-api-0.116.11+1.21.1.jar` | `fabric-api` | 0.116.11+1.21.1 | environment * |
| `fabric-language-kotlin-1.13.11+kotlin.2.3.21.jar` | `fabric-language-kotlin` | 1.13.11+kotlin.2.3.21 | environment * |
| `ferritecore-7.0.3-fabric.jar` | `ferritecore` | 7.0.3 | environment * |
| `fightorflight-fabric-0.10.7.jar` | `fightorflight` | 0.10.7 | environment * |
| `fzzy_config-0.7.6+1.21.jar` | `fzzy_config` | 0.7.6+1.21 | environment * |
| `geckolib-fabric-1.21.1-4.8.4.jar` | `geckolib` | 4.8.4 | environment * |
| `jei-1.21.1-fabric-19.27.0.340.jar` | `jei` | 19.27.0.340 | environment * |
| `krypton-0.2.8.jar` | `krypton` | 0.2.8 | environment * |
| `lithium-fabric-0.15.3+mc1.21.1.jar` | `lithium` | 0.15.3+mc1.21.1 | environment * |
| `mega_showdown-fabric-1.8.2+1.7.3+1.21.1-hotfix.jar` | `mega_showdown` | 1.8.2+1.7.3+1.21.1-hotfix | environment * |
| `modernfix-fabric-5.25.1+mc1.21.1.jar` | `modernfix` | 5.25.1+mc1.21.1 | environment * |
| `owo-lib-0.12.15.4+1.21.jar` | `owo` | 0.12.15.4+1.21 | environment * |
| `packetfixer-3.3.1-1.20.5-1.21.X-merged.jar` | `packetfixer` | 3.3.1 | environment * |
| `placeholder-api-2.4.2+1.21.jar` | `placeholder-api` | 2.4.2+1.21 | environment * |
| `shulkerboxtooltip-fabric-5.1.9+1.21.1.jar` | `shulkerboxtooltip` | 5.1.9+1.21.1 | environment * |
| `sound-physics-remastered-fabric-1.21.1-1.5.1.jar` | `sound_physics_remastered` | 1.21.1-1.5.1 | environment * |
| `togglenametags-2.1.1.jar` | `togglenametags` | 2.1.1 | environment * |
| `vmp-fabric-mc1.21.1-0.2.0+beta.7.172-all.jar` | `vmp` | 0.2.0+beta.7.172+1.21.1 | environment * |
| `xaerominimap-fabric-1.21.1-26.6.0.jar` | `xaerominimap` | 26.6.0 | environment * |
| `xaeroworldmap-fabric-1.21.1-1.47.0.jar` | `xaeroworldmap` | 1.47.0 | environment * |
| `zamega-fabric-1.7.3.jar` | `zamega` | 1.7.3 | environment * |
| `breeding-lifecycle-0.1.0+1.21.1.jar` | `breedinglifecycle` | 0.1.0+1.21.1 | this project |

### Excluded (37)
| File | Mod id | Version | Reason |
|---|---|---|---|
| `AmbientSounds_FABRIC_v6.3.6_mc1.21.1.jar` | `ambientsounds` | 6.3.6 | environment client |
| `BadOptimizations-2.4.1-1.21.1.jar` | `badoptimizations` | 2.4.1 | environment client |
| `BetterFullscreen-v2.3.0-mc1.21.1.jar` | `fullscreenfix` | 2.3.0 | environment client |
| `BetterThirdPerson-Fabric-1.21-1.9.0.jar` | `betterthirdperson` | 1.9.0 | environment client |
| `ImmediatelyFast-Fabric-1.6.10+1.21.1.jar` | `immediatelyfast` | 1.6.10+1.21.1 | environment client |
| `ImmersiveUI-FABRIC-0.3.3+1.21.1.jar` | `immersiveui` | 0.3.3 | environment client |
| `InventoryParticles-2.0.1+1.21.1+fabric.jar` | `inventory_particles` | 2.0.1+1.21.1+fabric | environment client |
| `ItemBorders-1.21-fabric-1.2.5.jar` | `itemborders` | 1.2.5 | environment client |
| `MouseTweaks-fabric-mc1.21-2.26.jar` | `mousetweaks` | 2.26 | environment client |
| `Perception-FABRIC-0.2.1+1.21.1.jar` | `perception` | 0.2.1 | environment client |
| `PresenceFootsteps-1.10.2+1.21.jar` | `presencefootsteps` | 1.10.2+1.21 | environment client |
| `Prism-1.21.1-fabric-1.0.11.jar` | `prism` | 1.0.11 | environment client |
| `RecallBeams-0.1.0-onecolor.jar` | `recallbeams` | 0.1.0-onecolor | environment client |
| `Too Many Entities 1.21.1 Fabric v1.2.0.jar` | `too_many_entities` | 1.2.0 | environment client |
| `TooManyPlayers-1.21-1.1.5.jar` | `toomanyplayers` | 1.21-1.1.5 | environment client |
| `bobby-5.2.4.1+mc1.21.jar` | `bobby` | 5.2.4.1+mc1.21 | environment client |
| `chat_heads-0.15.1-fabric-1.21.jar` | `chat_heads` | 0.15.1 | environment client |
| `chatanimation-fabric-1.21.1-1.1.3.jar` | `chatanimation` | 1.1.3 | environment client |
| `cinematiczoom-0.8.0.jar` | `cinematiczoom` | 0.8.0 | environment client |
| the pack's own client-side mod | — | 1.7.12 | environment client |
| `entity_model_features_1.21-fabric-3.2.2.jar` | `entity_model_features` | 3.2.2 | environment client |
| `entity_texture_features_1.21-fabric-7.1.jar` | `entity_texture_features` | 7.1 | environment client |
| `fallingleaves-1.17.1+1.21.1.jar` | `fallingleaves` | 1.17.1 | environment client |
| `fast-ip-ping-v1.0.11-mc1.21.1-fabric.jar` | `fastipping` | 1.0.11 | environment client |
| `iris-fabric-1.8.8+mc1.21.1.jar` | `iris` | 1.8.8+mc1.21.1 | environment client |
| `itemzoomer-fabric-2.0.1-1.21.1.jar` | `itemzoomer` | 2.0.1-1.21.1 | environment client |
| `language-reload-1.7.6+1.21.1.jar` | `languagereload` | 1.7.6+1.21.1 | environment client |
| `modmenu-11.0.4.jar` | `modmenu` | 11.0.4 | environment client |
| `moreculling-fabric-1.21.1-1.0.7.jar` | `moreculling` | 1.0.7 | environment client |
| `perspectivenametag-fabric-1.2.0+1.15.jar` | `perspectivenametag` | 1.2.0+1.15 | environment client |
| `punchy-2.5.4-fabric-1.21.1.jar` | `punchy` | 1.21.1 | environment client |
| `reeses-sodium-options-fabric-1.8.3+mc1.21.4.jar` | `reeses-sodium-options` | 1.8.3+mc1.21.4 | environment client |
| `smart_particles+mc1.21.1-1.01.10.jar` | `smart_particles` | 1.01.10 | environment client |
| `smoothswapping-0.9.3.2-1.21.1-fabric.jar` | `smoothswapping` | 0.9.3.2 | environment client |
| `sodium-extra-fabric-0.6.0+mc1.21.1.jar` | `sodium-extra` | 0.6.0+mc1.21.1 | environment client |
| `sodium-fabric-0.6.13+mc1.21.1.jar` | `sodium` | 0.6.13+mc1.21.1 | environment client |
| `visuality-0.7.7+1.21.jar` | `visuality` | 0.7.7+1.21 | environment client |

### The pack's client mod
The pack's own mod declares `"environment": "client"` in its `fabric.mod.json`, so it is client-only and was not
put on the server. Nothing else in it was read.

## Console tests
Sent through the console FIFO; output as the server logged it, timestamps removed. The config the mod writes on its
first start is shown first: the default preset, daycare off. `start.sh` then switches the daycare on.

```
> generated config (config/breeding-lifecycle.generated.json)
{
  "preset": "default",
  "presets": {
    "default": {
      "p1": 0.33,
      "r": 0.764,
      "maxBreedings": null,
      "ditto": { "p1": 0.33, "r": 0.764, "maxBreedings": null }
    }
  },
  "overrides": {},
  "referenceDaycare": { "enabled": false },
  "eggEventAdapter": { "enabled": false },
  "audit": { "enabled": true }
}
> breedinglifecycle curve
Active curve: preset default
Main curve: p1 = 0.3300, r = 0.7640, maxBreedings = none
n | p(n) | S(n)
1 | 0.3300 | 1.0000
2 | 0.4881 | 0.6700
3 | 0.6089 | 0.3430
4 | 0.7012 | 0.1341
5 | 0.7717 | 0.0401
6 | 0.8256 | 0.0091
7 | 0.8668 | 0.0016
8 | 0.8982 | 0.0002
Expected offspring per parent: E = 2.1981
Ditto curve: p1 = 0.3300, r = 0.7640, maxBreedings = none
n | p(n) | S(n)
1 | 0.3300 | 1.0000
2 | 0.4881 | 0.6700
3 | 0.6089 | 0.3430
4 | 0.7012 | 0.1341
5 | 0.7717 | 0.0401
6 | 0.8256 | 0.0091
7 | 0.8668 | 0.0016
8 | 0.8982 | 0.0002
Expected offspring per parent: E = 2.1981
> breedinglifecycle reload (valid: reset.sh)
Breeding Lifecycle config reloaded.
> breedinglifecycle reload (invalid: presets.default.p1 = 2.0)
Config not reloaded, the previous one stays active. Errors:
- presets.default.p1 = 2.0: must be in [0, 1]
> breedinglifecycle curve (after the invalid reload)
Active curve: preset default
Main curve: p1 = 0.3300, r = 0.7640, maxBreedings = none
n | p(n) | S(n)
1 | 0.3300 | 1.0000
2 | 0.4881 | 0.6700
3 | 0.6089 | 0.3430
4 | 0.7012 | 0.1341
5 | 0.7717 | 0.0401
6 | 0.8256 | 0.0091
7 | 0.8668 | 0.0016
8 | 0.8982 | 0.0002
Expected offspring per parent: E = 2.1981
Ditto curve: p1 = 0.3300, r = 0.7640, maxBreedings = none
n | p(n) | S(n)
1 | 0.3300 | 1.0000
2 | 0.4881 | 0.6700
3 | 0.6089 | 0.3430
4 | 0.7012 | 0.1341
5 | 0.7717 | 0.0401
6 | 0.8256 | 0.0091
7 | 0.8668 | 0.0016
8 | 0.8982 | 0.0002
Expected offspring per parent: E = 2.1981
> loss.sh
Breeding Lifecycle config reloaded.
> reset.sh
Breeding Lifecycle config reloaded.
> grep -iE "breedinglifecycle.*(WARN|ERROR)|mixin.*conflict" logs/latest.log
(nothing)
> grep -iE "breeding.?lifecycle" logs/latest.log | grep -E "WARN|ERROR"
(nothing)
> command conflicts
(nothing)
```

## Log review
- Nothing from Breeding Lifecycle at WARN or ERROR. A first boot showed
  `Mod io_github_bernhardtwo_breedinglifecycle_core uses the version unspecified`: the nested `core` jar had no
  version. `core` is now versioned `0.1.0` and the warning is gone.
- No command conflicts. The only mixin conflicts are two `Method overwrite conflict` warnings between modernfix and
  lithium (`BiomeMixin`, `SortedArraySetMixin`); this mod has no mixins.
- Errors from other mods, harmless for this test: `dynamic-lighting-1.0.jar` ships 7 functions (`dynlight:`) that
  fail to parse on this version, and Cobblemon and Fight or Flight log `No data fixer registered` for their entities.

## Still to verify with a client
The development client now covers, in English, `/daycare deposit`, `breed`, `status`, `withdraw`,
`/breedinglifecycle info`, a loss with `p1 = 1.0` and `deposit` refused while the Pokémon is out of its Poké Ball
([in-game screenshots](ingame-screenshots.md)). Still open, with the real CurseForge client on this server:
- the same commands next to the pack's other mods, and in Spanish;
- `/daycare withdraw` refused during a battle.
