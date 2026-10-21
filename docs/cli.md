# CLI

Terminal simulator on top of `core`. Build it with `./gradlew :cli:installDist` and run
`cli/build/install/cli/bin/cli <command>`.

## Commands
| Command | Output |
|---|---|
| `curve` | `n`, `p(n)`, `S(n)` until `S(n)` rounds to 0 (or up to `maxBreedings`), then `E`. |
| `simulate [--parents N] [--seed SEED]` | Monte Carlo over `N` parents (default 10000): mean offspring per parent next to the exact `E`, p50/p90/p99/max and the distribution. Without `--seed`, a random seed is used and printed so the run can be repeated. |
| `compare` | The curve against the current system (one offspring per parent, two per pair), with the difference. |

## Shared options
| Option | Meaning |
|---|---|
| `--config FILE` | The same JSON the mod reads ([configuration](configuration.md)); the effective curves after `overrides` are used. Default: the built-in `default` preset. |
| `--ditto` | Use the Ditto curve instead of the main one. |
| `--lang es\|en` | Output language and number format. Default: Spanish when the system language is Spanish (`es`, `es-MX`, …), English otherwise. |

## Exit codes
`0` success · `2` usage error, invalid `--lang`, `--parents < 1` or an invalid/unreadable config (each error is
printed on stderr) · `1` unexpected failure.

## Example
```text
$ cli curve --lang en
Curve (main curve): p1 = 0.3300, r = 0.7640, maxBreedings = none

   n      p(n)      S(n)
   1    0.3300    1.0000
   2    0.4881    0.6700
   3    0.6089    0.3430
   4    0.7012    0.1341
   5    0.7717    0.0401
   6    0.8256    0.0091
   7    0.8668    0.0016
   8    0.8982    0.0002

Expected offspring per parent:
E = 2.1981
```

```text
$ cli compare --lang es
Curva (curva principal): p1 = 0,3300, r = 0,7640, maxBreedings = sin límite

                               Crías/padre      Crías/pareja
Sistema actual                      1,0000            2,0000
Ciclo de crianza                    2,1981            4,3963

Diferencia por padre: +1,1981 (+119,8%)
```
