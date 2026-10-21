# Formula

Every breeding always produces the offspring. Afterwards each parent rolls for loss on its own, with its own
breeding number `n` (1 for its first breeding) and its own curve.

## Loss curve
`p(n) = 1 − (1 − p1) · r^(n−1)`, and `p(n) = 1` when `maxBreedings` is set and `n ≥ maxBreedings`.

- `p1` ∈ [0, 1]: loss chance on the first breeding.
- `r` ∈ (0, 1]: how fast the chance of keeping the parent shrinks; `r = 1` keeps `p(n) = p1`.
- Without `maxBreedings`, a curve must lose its parent eventually: `p1 = 0` with `r = 1` is rejected, and so is
  any curve whose `S(n)` is still above `1e-12` after 1,000,000 breedings (for example `p1 = 1e-17`, `r = 1`).

## Survival and expected offspring
`S(1) = 1`, `S(n+1) = S(n) · (1 − p(n))` is the chance the parent is still there for breeding `n`.
`E = Σ S(n)` is the expected offspring per parent, summed until `S(n) < 1e-12` or up to `maxBreedings`.
The current system (both parents always consumed) is `E = 1`.

## Default preset (`p1 = 0.33`, `r = 0.764`)
| n | p(n) | S(n) |
|---|---|---|
| 1 | 0.3300 | 1.0000 |
| 2 | 0.4881 | 0.6700 |
| 3 | 0.6089 | 0.3430 |
| 4 | 0.7012 | 0.1341 |
| 5 | 0.7717 | 0.0401 |

`E = 2.1981`. A Ditto uses its own `p1`/`r`/`maxBreedings`, with the same default values.

## Shared test vectors
`test-vectors/curve-cases.json` is written by `python3 scripts/gen-curve-vectors.py`, a separate implementation
of the formula. The `core` tests (and later the `web` tests) must match every `p`, `S` and `E` within `1e-9`.
Regenerate it only when a case is added.

## Rolls and simulation
- After every breeding, each parent rolls `u` uniform in `[0, 1)`; it is lost when `u < p(n)`, kept otherwise.
  The two parents roll independently, each with its own `n` and curve.
- `core` takes the random source as `RandomSource`; `SeededRandom(seed)` reproduces any run.
- `Simulator.run(curve, parents, rng)` breeds each parent until it is lost and reports the mean offspring per
  parent, the nearest-rank p50/p90/p99, the maximum and the histogram. The mean converges to `E`.
