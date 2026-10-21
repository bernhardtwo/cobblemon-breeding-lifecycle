# Screenshots

Regenerate the CLI ones with `./scripts/cli-screenshots.sh` (needs `freeze`: `brew install charmbracelet/tap/freeze`).

| File | Shows | Works |
|---|---|---|
| `cli-curve-en.png` | `cli curve --lang en`: default curve table, `E = 2.1981` | yes |
| `cli-curve-es.png` | `cli curve --lang es`: same table with Spanish labels and decimal commas | yes |
| `cli-simulate-en.png` | `cli simulate --lang en --parents 10000 --seed 42`: mean, percentiles, distribution | yes |
| `cli-simulate-es.png` | `cli simulate --lang es --parents 10000 --seed 42` | yes |
| `cli-compare-en.png` | `cli compare --lang en`: current system (1 per parent) vs the curve (2.1981) | yes |
| `cli-compare-es.png` | `cli compare --lang es` | yes |
| `ingame-daycare-status.png` | `/daycare status` in a dev client: parents, breedings, next loss chance, offspring | not taken: see [TODO.md](TODO.md) |
| `ingame-info.png` | `/breedinglifecycle info 1` in a dev client | not taken: see [TODO.md](TODO.md) |
| `ingame-loss.png` | `/daycare breed` with `p1 = 1.0`: both parents lost | not taken: see [TODO.md](TODO.md) |

The in-game ones are set up with `./scripts/ingame-screenshots.sh server|client|loss` and taken by hand, as
[TODO.md](TODO.md) says.
