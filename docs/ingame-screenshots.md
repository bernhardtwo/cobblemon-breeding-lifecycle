# In-game screenshots

The four `ingame-*.png` in [screenshots/](screenshots/INDEX.md) are taken by the development client itself, with no
input from anyone and nothing typed into any window ([0012](decisions/0012-ingame-screenshots-dev-client.md)).

## Regenerate
```bash
scripts/ingame-screenshots.sh auto
```
JDK 21 is needed (the script finds it as `scripts/check.sh` does). A Minecraft window opens and closes on its own;
leave it alone, the rest of the desktop stays usable. It takes about five minutes and ends with
`4 screenshots in docs/screenshots` and exit code 0.

On any failure it prints the reason and exits 1, for example
`[screenshots] FAILED: timed out waiting for 'Waiting offspring (1): Charmander' after /daycare status`, or
`timed out waiting for: [screenshots] DONE` after 20 minutes. Either way the server and the client are stopped and
`mod/run/config/breeding-lifecycle.json` goes back to the default curve (`"overrides": {}`).

## What it does
1. Starts the dev server of `scripts/ingame-screenshots.sh server` in `mod/run`: offline, flat world, creative,
   `Dev` as operator, reference daycare on.
2. Starts `./gradlew :mod:runClient -Pbreedinglifecycle.screenshots=true` in `mod/run-client`, as `Dev`, joining
   `localhost:25565`, 1600×900. The Gradle properties reach the game as `-D` system properties.
3. The client mod in `mod/src/screenshots` sets English and GUI scale 2, then plays four scenes. Each sends its
   chat commands one at a time and waits up to 30 s for the server's reply before the next one:

| Scene | Commands | Waits for |
|---|---|---|
| `ingame-daycare-status` | `daycare deposit` ×2, `daycare breed`, `daycare status` | `Waiting offspring (1): Charmander` |
| `ingame-info` | `daycare withdraw`, `breedinglifecycle info <slot of a parent that came back>` | `breedings: 1, loss chance on the next breeding: 0.4881` |
| `ingame-loss` | config with `"overrides": { "p1": 1.0 }`, `breedinglifecycle reload`, `daycare deposit` ×2, `daycare breed` | `Parent 2 is gone: breeding 1, loss chance 1.0000` |
| `ingame-refused` | sends a Charmander out (Cobblemon's own send-out packet), `daycare deposit <its slot>` | `is out of its Poké Ball` |

   Every scene starts from `daycare withdraw`, `clearparty Dev` and a new male and female Charmander, so leftovers
   of an earlier run do not matter. If both parents are lost in the first scene (about 11 % on the default curve),
   it tries again, up to five times.
4. Each screenshot is taken with Minecraft's own `Screenshot.grab` with the chat open, then copied to
   `docs/screenshots/`. The client puts the default config back, reloads it, logs `[screenshots] DONE` and quits.

## What they are not
They come from the development client: Cobblemon 1.7.3 and this mod, without the CurseForge pack's other mods, and
in English only. The Spanish texts and the pack together are covered by [live-test](live-test.md), not here.

## Kept out of the mod
The screenshots client is its own source set and Fabric mod (`breedinglifecycle-screenshots`, client only). Without
`-Dbreedinglifecycle.screenshots=true` it does nothing, and it is never packed: `./gradlew check` runs
`checkDistributedJar`, which fails if any of it is in `mod/build/libs/breeding-lifecycle-<version>.jar`.
