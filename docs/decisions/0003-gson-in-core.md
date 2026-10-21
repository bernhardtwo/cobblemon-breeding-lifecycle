# 0003 · Gson is core's only dependency

Status: accepted (2026-10-21)

## Context
The CLI and the mod must accept and reject exactly the same config, so parsing and validation belong in `core`.
Writing a JSON parser by hand would be more code to trust.

## Decision
`core` depends on `com.google.code.gson:gson:2.10.1`, the version Minecraft 1.21.1 ships
([1.21.1 version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), library
`com.google.code.gson:gson:2.10.1`). The mod uses the Gson already on the server classpath; the CLI bundles it.
The parser works on `JsonObject` rather than binding to classes, so it can tell an absent field from `null`.

## Consequences
`core` stays free of Minecraft. If a future Minecraft version ships another Gson, `core` follows it.
