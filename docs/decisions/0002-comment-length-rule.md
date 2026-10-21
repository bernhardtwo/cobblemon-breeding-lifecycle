# 0002 · Comment length is measured per comment

Status: accepted (2026-10-21)

## Context
The repo rule says no comment may exceed 200 characters and forbids long comment blocks. Checkstyle's regex checks
work per line and cannot read TypeScript ([java-style-tooling](../research/java-style-tooling.md)).

## Decision
`checkCommentLength` (in `build-logic`, part of `./gradlew check`) scans `**/src/**/*.java` and `web/src/**/*.ts(x)`.
A comment is measured whole: markers and leading `*` are removed, blank lines dropped and the remaining lines joined
with one space. Consecutive standalone `//` lines count as one comment. Strings, text blocks and template literals
are skipped.

## Consequences
A Javadoc block longer than 200 characters fails the build, which keeps public Javadoc brief.
