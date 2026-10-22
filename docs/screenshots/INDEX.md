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
| `ingame-daycare-status.png` | `/daycare deposit` ×2, `breed`, `status` on the default curve: who stayed, `breedings: 1`, next loss chance `0.4881`, `Waiting offspring (1): Charmander` | yes |
| `ingame-info.png` | `/daycare withdraw`, then `/breedinglifecycle info <slot>` of a parent that came back: `breedings: 1, loss chance on the next breeding: 0.4881` | yes |
| `ingame-loss.png` | `/daycare breed` with `"overrides": { "p1": 1.0 }`: `Parent 1 is gone` and `Parent 2 is gone`, each with `loss chance 1.0000` | yes |
| `ingame-refused.png` | `/daycare deposit` of a Charmander out of its Poké Ball: `Charmander is out of its Poké Ball. Return it before leaving it at the daycare.` | yes |

Regenerate the in-game ones with `scripts/ingame-screenshots.sh auto` (JDK 21; a Minecraft window opens and closes
on its own, about five minutes). They come from the development client, `./gradlew :mod:runClient`, with Cobblemon
and this mod only, not the CurseForge pack's other mods; the client is English, 1600×900, GUI scale 2.
How it works: [in-game screenshots](../ingame-screenshots.md).

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
