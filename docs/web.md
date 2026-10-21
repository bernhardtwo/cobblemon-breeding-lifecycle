# Web simulator

`web/` is a static Vite + TypeScript page with no backend: every number is computed in the browser.

## Use
```bash
npm --prefix web ci
npm --prefix web run dev                       # http://localhost:5173
npm --prefix web run build                     # web/dist, base "/"
WEB_BASE=/breeding/ npm --prefix web run build # served under /breeding/
npm --prefix web run preview                   # serves web/dist
```
The page is not deployed anywhere; `web/dist` can be served by any static host.

- Two curves, main and Ditto, each with sliders for `p1` (0–1, step 0.01) and `r` (0.01–1, step 0.001) and an
  optional `maxBreedings` (the "No limit" box, or a number from 1 to 20).
- `E` for both curves against `1.00`, the current system of one offspring per parent; charts of `p(n)` and `S(n)` for
  n = 1..12 and a table for n = 1..8.
- **Export JSON** downloads `breeding-lifecycle.json`; **Copy** copies it. Both show it in the export panel. The file
  is the [config](configuration.md) with these curves as the `default` preset, empty `overrides` and the mod blocks at
  their defaults, so `cli curve --config breeding-lifecycle.json` and the mod accept it. With no limit it writes
  `"maxBreedings": null`, and numbers always use a dot.
- An invalid curve (for example `p1 = 0`, `r = 1` with no limit) shows the reason, leaves its `E` as `—` and disables
  the export.

## Language and theme
- `?lang=es` or `?lang=en` (any case) wins. Otherwise Spanish when `navigator.language` starts with `es`, else English;
  an unknown `?lang=` falls back to that rule. The ES/EN buttons switch at any time.
- Spanish shows decimal commas (`2,20`) in `E`, the sliders, the charts and the table; the exported JSON keeps dots.
- The theme follows `prefers-color-scheme`; the Light/Dark buttons override it and the choice is remembered in
  `localStorage` (`breeding-lifecycle-theme`). If storage throws or holds anything else, the system theme is used.
- Visual identity from the author's portfolio (Funnel Display, Funnel Sans, Archivo, accent `#3fe0c5`, dark background
  `#0b0b0b`); the light palette is derived in [0010](decisions/0010-web-light-palette.md).

## Exact calculation
`web/src/curve.ts` is a line-for-line port of `core`'s `Curve`: `p(n) = 1 − (1 − p1) · r^(n−1)`, `p(n) = 1` from
`maxBreedings`, `E = Σ S(n)` until `S(n) < 1e-12` or `maxBreedings`, and the same validation messages. Vitest runs it
against every case of `test-vectors/curve-cases.json` with an absolute tolerance of `1e-9`, the same file the `core`
JUnit tests read. Messages print numbers the JavaScript way (`r = 0`, where Java prints `r = 0.0`).

`test-vectors/web-export-example.json` is the default export: the web test checks that `toConfigJson` produces it and
`core`'s `WebExportTest` checks that `ConfigParser` accepts it.

## Tests
`npm --prefix web test` (Vitest): curve vectors, config export, language and theme resolution, and the page itself in
jsdom (`test/page.test.ts`). `./scripts/check.sh` runs `npm ci`, the tests and the build after `./gradlew check`.
