# 0010 · Web light palette derived from the dark portfolio palette

Status: accepted (2026-10-21)

## Context
S13 asks for the portfolio's identity (Funnel Display, Funnel Sans, Archivo, accent `#3fe0c5`, background `#0b0b0b`)
in a light and a dark theme. The portfolio is dark only, and `#3fe0c5` on a light background has a contrast of about
1.5:1, below WCAG AA for text and lines.

## Decision
- Dark theme: the portfolio tokens as they are (`bg #0b0b0b`, `surface #1a1a19`, `line #3a3a37`, `ink #f2f0ea`,
  `muted #8f8c84`, accent `#3fe0c5`).
- Light theme: warm off-white `bg #f7f6f2`, white surfaces, `line #d4d1c8`, `ink #16181a`, `muted #5f5c55` (6.2:1).
- The accent splits in two roles: `--accent-fill` stays `#3fe0c5` with `#0b0b0b` text on buttons (11.9:1) in both
  themes; `--accent`, used for text, chart lines and the focus ring, is `#0b7a69` in light (4.9:1 on the background).
- The Ditto curve gets a warm second colour (`#f0a868` dark, `#a3560f` light) and a dashed line, so the two series
  are told apart without colour.

## Consequences
- The light page keeps the brand through the button fill and the teal family while meeting AA.
- Any new token needs a value in both themes in `web/src/styles.css`.
