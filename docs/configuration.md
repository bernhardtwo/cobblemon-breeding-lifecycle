# Configuration

One JSON file, `config/breeding-lifecycle.json` on the server, also read by `cli --config <path>`.
`core` parses and validates it (`ConfigParser`), so the CLI and the mod accept and reject exactly the same files.
When the file is missing, the mod writes this default:

```json
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
```

## Fields
| Field | Meaning |
|---|---|
| `preset` | Name of the preset in use. Defaults to `default` when absent. |
| `presets.<name>` | A curve: `p1` and `r` required, `maxBreedings` optional (absent means `null`), `ditto` optional. |
| `presets.<name>.ditto` | The curve a Ditto parent uses. When absent it copies the preset's main curve. |
| `overrides` | Same fields as a preset, all optional; replaces field by field on top of the selected preset. |
| `referenceDaycare`, `eggEventAdapter`, `audit` | Mod-only switches; the CLI ignores them. |

See [formula](formula.md) for what `p1`, `r` and `maxBreedings` do.

## Mod switches
| Block | Default | Effect |
|---|---|---|
| `referenceDaycare.enabled` | `false` | Enables the reference `/daycare` (milestone 3). |
| `eggEventAdapter.enabled` | `false` | Applies the loss to `COLLECT_EGG` from breeding mods ([integration](integration.md)). |
| `audit.enabled` | `true` | Writes `logs/breeding-lifecycle/audit.jsonl`. |

- A missing block, or a block without `enabled`, uses the default.
- A block that is not an object, or an `enabled` that is not a boolean, is a config error:
  `audit.enabled = "yes": must be true or false`, `eggEventAdapter = true: must be an object`.
- Curve errors and switch errors are reported together.

## Audit log
With `audit.enabled`, every evaluated parent adds one JSON line to `logs/breeding-lifecycle/audit.jsonl`, written
on a background thread (`breedinglifecycle-audit`) so the server thread never waits on disk:

```json
{"time":"2026-10-21T15:00:00Z","playerUuid":"…","playerName":"Ash","pokemonUuid":"…","species":"cobblemon:bulbasaur","n":3,"p":0.6089,"roll":0.25,"result":"lost","source":"daycare"}
```

`time` is ISO-8601 UTC; `playerUuid` and `playerName` are `null` when the owner is unknown (a wild Pokémon, an
owner never seen by the server); `result` is `kept`, `lost` or `skipped`; `source` is `api`, `daycare` or `egg-event`.

## Loading and reload (mod)
- At start the mod reads `config/breeding-lifecycle.json`, writing the default above when the file is missing.
  An invalid file at start logs every error and the server runs with the defaults.
- `/breedinglifecycle reload` and vanilla `/reload` re-read the file. A valid file replaces the settings at once;
  an invalid one keeps the previous settings and the errors are shown to whoever ran the command.

## Overrides
- Each field present in `overrides` replaces the same field of the selected preset; the preset itself is unchanged.
- `overrides.ditto` merges field by field into the preset's Ditto curve.
- An explicit `"maxBreedings": null` removes the preset's limit; leaving the field out keeps the preset's value.

## Validation
Every error is reported at once, each naming the field path, the value and the rule:

| Rule | Example message |
|---|---|
| `p1` in [0, 1] | `overrides.p1 = 2: must be in [0, 1]` |
| `r` in (0, 1] | `presets.default.ditto.r = 1.2: must be in (0, 1]` |
| `maxBreedings` null or integer ≥ 1 | `overrides.maxBreedings = 2.5: must be null or an integer >= 1` |
| numbers must be numbers | `overrides.p1 = "high": must be a number` |
| a curve must lose parents eventually | `overrides: p1 = 0 and r = 1: a parent would never be lost; set maxBreedings` |
| … within 1,000,000 breedings | `overrides: p1 = 1.0E-17 and r = 1.0: a parent would practically never be lost (…); set maxBreedings` |
| required fields | `presets.fast.r: required` |
| known fields only | `overrides.p2: unknown field` |
| selected preset exists | `preset = nope: no such preset in presets` |
| valid JSON | `invalid JSON: …` |
