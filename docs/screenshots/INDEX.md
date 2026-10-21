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

## Web

Regenerate with `npm --prefix web run screenshots` (Playwright Chromium against `vite preview` of the build; install
the browser once with `npx --prefix web playwright install chromium`). The script fails if any page scrolls sideways.
Names are `web-<width>-<theme>-<lang>-<state>.png`: widths 390 and 1440, themes `light` and `dark`
(`prefers-color-scheme`), languages `es` and `en` (`?lang=`), 24 files in all.

| Files | Shows | Works |
|---|---|---|
| `web-{390,1440}-{light,dark}-{es,en}-initial.png` | Default curves: `E = 2.20` (`2,20` in es) vs `1.00`, Ditto `2.20`, both charts and the table | yes |
| `web-{390,1440}-{light,dark}-{es,en}-sliders.png` | Main curve moved to `p1 = 0.5`, `r = 0.9`: `E` and the main series change, Ditto stays | yes |
| `web-{390,1440}-{light,dark}-{es,en}-exported.png` | After **Export JSON**: the panel shows the exported `breeding-lifecycle.json` with `"maxBreedings": null` | yes |
