# In-game screenshots to take

Not taken automatically: driving the client means typing chat commands with `osascript` into whatever window is
in front, which is unsafe on a desktop someone is using. Take these three by hand; the world setup is scripted.

## World setup
1. Terminal 1: `scripts/ingame-screenshots.sh server`. It writes `mod/run/server.properties` (`online-mode=false`,
   flat world, creative), makes `Dev` an operator, and writes `mod/run/config/breeding-lifecycle.json` with
   `"referenceDaycare": { "enabled": true }` and `"overrides": {}`.
2. Terminal 2, once terminal 1 says `server ready`: `scripts/ingame-screenshots.sh client`. The dev client joins
   `localhost:25565` as `Dev`.
3. When `Dev` joins, the server gives it a male and a female Charmander, in party slots 1 and 2.
4. Set the client language to English, hide nothing; take each screenshot of the Minecraft window with
   `screencapture -l <window id> -o <file>` or `Cmd+Shift+4`, then `Space`, with the chat still open (`T`).

## 1. `ingame-daycare-status.png`
Type in chat, in this order:
```
/daycare deposit 1
/daycare deposit 2
/daycare breed
/daycare status
```
Capture after `/daycare status`: two Charmander lines with their breedings (1 each if both stayed, fewer lines if
one was lost on the default curve), the next loss chance, and `Waiting offspring (1): Charmander`.

## 2. `ingame-info.png`
```
/daycare withdraw
/breedinglifecycle info 1
```
Capture after `info`: `Slot 1: Charmander, breedings: 1, loss chance on the next breeding: 0.4881` (or the slot of
a parent that came back).

## 3. `ingame-loss.png`
1. Terminal 3: `scripts/ingame-screenshots.sh loss` (writes `"overrides": { "p1": 1.0 }` and runs
   `breedinglifecycle reload` on the server console).
2. In chat, with a male and a female Charmander in slots 1 and 2 (move them there from the party screen if needed):
```
/daycare deposit 1
/daycare deposit 2
/daycare breed
```
Capture after `breed`: `A Charmander was born...`, then `Parent 1 is gone` and `Parent 2 is gone`, each with
`loss chance 1.0000`.

When done, add the three rows to [INDEX.md](INDEX.md) and delete this file.
