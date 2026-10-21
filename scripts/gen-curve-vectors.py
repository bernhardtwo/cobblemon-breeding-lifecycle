"""Writes test-vectors/curve-cases.json from the loss-curve formula, independently of core."""

import json
from pathlib import Path

CASES = [
    ("default", 0.33, 0.764, None),
    ("max-breedings-3", 0.33, 0.764, 3),
    ("r-one", 0.25, 1.0, None),
    ("p1-one", 1.0, 0.5, None),
    ("ditto-custom", 0.2, 0.9, None),
]
SHOWN = 10
EPSILON = 1e-12


def loss(p1, r, max_breedings, n):
    if max_breedings is not None and n >= max_breedings:
        return 1.0
    return 1.0 - (1.0 - p1) * r ** (n - 1)


def case(name, p1, r, max_breedings):
    p = [loss(p1, r, max_breedings, n) for n in range(1, SHOWN + 1)]
    s = [1.0]
    for n in range(1, SHOWN):
        s.append(s[-1] * (1.0 - p[n - 1]))
    expected, survival, n = 0.0, 1.0, 1
    while survival >= EPSILON and (max_breedings is None or n <= max_breedings):
        expected += survival
        survival *= 1.0 - loss(p1, r, max_breedings, n)
        n += 1
    return {
        "name": name,
        "curve": {"p1": p1, "r": r, "maxBreedings": max_breedings},
        "p": p,
        "S": s,
        "E": expected,
    }


def main():
    out = Path(__file__).resolve().parent.parent / "test-vectors" / "curve-cases.json"
    data = {"tolerance": 1e-9, "cases": [case(*c) for c in CASES]}
    out.write_text(json.dumps(data, indent=2) + "\n")


if __name__ == "__main__":
    main()
