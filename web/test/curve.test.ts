import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { type Curve, DEFAULT_CURVE, expectedOffspring, lossProbability, survival, validateCurve } from '../src/curve';

type Vector = { name: string; curve: Curve; p: number[]; S: number[]; E: number };

const vectors = JSON.parse(readFileSync(new URL('../../test-vectors/curve-cases.json', import.meta.url), 'utf8')) as { cases: Vector[] };

describe.each(vectors.cases)('$name', (v) => {
  it('matches p, S and E within 1e-9', () => {
    v.p.forEach((p: number, i: number) => expect(Math.abs(lossProbability(v.curve, i + 1) - p)).toBeLessThan(1e-9));
    v.S.forEach((s: number, i: number) => expect(Math.abs(survival(v.curve, i + 1) - s)).toBeLessThan(1e-9));
    expect(Math.abs(expectedOffspring(v.curve) - v.E)).toBeLessThan(1e-9);
  });
});

it('rejectsNeverLosingCurve', () => expect(validateCurve({ p1: 0, r: 1, maxBreedings: null })[0]).toContain('set maxBreedings'));
it('rejectsNearZeroLoss', () => expect(validateCurve({ p1: 1e-17, r: 1, maxBreedings: null })[0]).toContain('practically never'));
it('rangeMessagesMatchCore', () => expect(validateCurve({ p1: 0.33, r: 0, maxBreedings: null })).toEqual(['r = 0: must be in (0, 1]']));
it('defaultIsBaseline', () => expect(expectedOffspring(DEFAULT_CURVE)).toBeCloseTo(2.1981, 4));
