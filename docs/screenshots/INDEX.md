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
