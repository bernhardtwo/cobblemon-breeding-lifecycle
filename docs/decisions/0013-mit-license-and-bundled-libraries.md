# 0013 · MIT license and bundled libraries

Status: accepted (2026-10-27)

## Context
The repository is about to be made public and the mod published on Modrinth and CurseForge. It had no license, and
the distributed jar nests third-party code whose terms must travel with it.

## Decision
- The project is MIT, `Copyright (c) 2026 Bernardo Vega`, in `LICENSE`; the `jar` task copies it into the jar root.
- Server Translations API `2.3.1+1.21-pre2` (MIT, CatCore) stays bundled with `include` (see
  [0007](0007-server-side-translations.md)). Its jar has no license file, so `THIRD-PARTY-NOTICES.md` carries its
  full MIT text.
- Packet Tweaker `0.5.1+24w03b` (LGPLv3, Gegy and Patbox) reaches the jar nested, unmodified, inside Server
  Translations API, with its own `LICENSE_packet-tweaker`. It is declared in `THIRD-PARTY-NOTICES.md` instead of being
  removed, since Server Translations API uses it at runtime, and stays a separate jar a user can replace.
- Its source is pinned to the distributed version: tag `v0.5.1+24w03b` of `NucleoidMC/packet-tweaker`, commit
  `02e154cfd36e006f625d641526d1ef638cfde0cb`, not to the repository in general.
- The LGPLv3 incorporates the GPLv3, which the nested jar does not carry. `LICENSES/GPL-3.0.txt` (the official text)
  and `LICENSES/LGPL-3.0.txt` (copied unchanged from `LICENSE_packet-tweaker`) ship in the repository and in the jar.
- The jar is named `breeding-lifecycle-<version>.jar`, with version `0.1.0+1.21.1` in the jar and in `fabric.mod.json`.

## Consequences
- `checkDistributedJar` fails when `LICENSE`, either file of `LICENSES/` or the Server Translations API jar is missing
  from the distributed jar.
- A new bundled library needs its entry in `THIRD-PARTY-NOTICES.md` in the same change.
