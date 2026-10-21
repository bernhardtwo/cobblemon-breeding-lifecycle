# 0007 · Server-side translations with Server Translations API

Status: accepted (2026-10-21)

## Context
Every player-facing text must be in Spanish and English, but the mod is server-only, so clients have no lang file
for `breedinglifecycle.*` keys.

## Decision
- Messages are `Component.translatable("breedinglifecycle.…", args)`. Nucleoid's Server Translations API
  (`xyz.nucleoid:server-translations-api:2.3.1+1.21-pre2`, nested with `include`) resolves them per player from
  `data/breedinglifecycle/lang/*.json`, using the language the client reports.
- The library builds a language as its own file plus `en_us`, with no fallback from a regional variant to its base
  (read from `ServerTranslations.createLanguage` in the 2.3.1 jar). A player on `es_mx` would get English. So
  `processResources` copies `es_es.json` to `es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy` and `es_ve`, Minecraft's
  other Spanish locales; only `es_es.json` and `en_us.json` live in the sources.
- Numbers are formatted on the server with four decimals and a dot (`Locale.ROOT`), since the arguments are
  rendered before the client's language is known.
- The library nests `packet-tweaker`; Loom's dev runtime does not load nested jars, so `mod` adds it as
  `modLocalRuntime` for GameTests only.

## Consequences
- `LangFilesTest` checks that both files have the same keys, all namespaced, and that each Spanish variant ships.
- GameTests render the captured components with `Localization.text(component, target)` to check real output in
  `en_us` and `es_mx`.
