# Modpack versions

Read on 2026-10-21 from the local CurseForge instance of the target modpack
(`minecraftinstance.json` and each jar's `fabric.mod.json`), read-only.

| Component | Version | Source |
|---|---|---|
| Minecraft | 1.21.1 | `minecraftinstance.json` → `gameVersion` |
| Fabric Loader | 0.19.2 | `minecraftinstance.json` → `baseModLoader.name` = `fabric-0.19.2-1.21.1` |
| Cobblemon | 1.7.3+1.21.1 | `mods/Cobblemon-fabric-1.7.3+1.21.1.jar` → `fabric.mod.json` |
| Fabric API | 0.116.11+1.21.1 | `mods/fabric-api-0.116.11+1.21.1.jar` → `fabric.mod.json` |
| Fabric Language Kotlin | 1.13.11+kotlin.2.3.21 | `mods/fabric-language-kotlin-1.13.11+kotlin.2.3.21.jar` → `fabric.mod.json` |
| Java | 21 | Cobblemon `depends.java = "21"` |

Cobblemon 1.7.3 declares `fabricloader >=0.17.2`, `fabric-api >=0.116.6+1.21.1`,
`minecraft 1.21.1`, `java 21`.

These values are pinned in `gradle.properties`.
