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
