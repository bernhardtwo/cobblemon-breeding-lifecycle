# 0001 · Build toolchain

Status: accepted (2026-10-21)

## Context
The 1.21.1 Fabric template uses Loom 1.18, which is compiled for Java 25, so Gradle would have to run on JDK 25
([java-style-tooling](../research/java-style-tooling.md), [fabric-mod-practices](../research/fabric-mod-practices.md)).
The target server and every module run on Java 21.

## Decision
- Gradle 9.7.1 wrapper, Kotlin DSL, shared conventions in `build-logic/` (`blc.java-conventions`, `blc.root-checks`).
- Fabric Loom `net.fabricmc.fabric-loom-remap` 1.17.x for `mod`, so one JDK 21 runs the whole build.
- Spotless 8.10.4 with palantir-java-format 2.102.0; Checkstyle 14.3.0 with NeedBraces, EmptyCatchBlock,
  EqualsHashCode, HideUtilityClassConstructor, AvoidStarImport and MissingJavadocType (public scope).
- JUnit 6.1.3 and AssertJ 3.27.7.

## Consequences
Moving to Loom 1.18+ later means running Gradle on JDK 25 while still compiling with `release = 21`.
