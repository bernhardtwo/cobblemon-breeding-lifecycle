import { readFileSync } from 'node:fs';
import { expect, it } from 'vitest';
import { toConfigJson } from '../src/config';
import { DEFAULT_CURVE } from '../src/curve';

const example = JSON.parse(readFileSync(new URL('../../test-vectors/web-export-example.json', import.meta.url), 'utf8'));

it('defaultExportMatchesCoreDefaultShape', () => expect(JSON.parse(toConfigJson(DEFAULT_CURVE, DEFAULT_CURVE))).toEqual(example));
it('exportWritesNullWhenNoLimit', () => expect(JSON.parse(toConfigJson(DEFAULT_CURVE, DEFAULT_CURVE)).presets.default.maxBreedings).toBeNull());
it('exportKeepsDittoCurve', () =>
  expect(JSON.parse(toConfigJson(DEFAULT_CURVE, { p1: 0.5, r: 0.9, maxBreedings: 4 })).presets.default.ditto).toEqual({ p1: 0.5, r: 0.9, maxBreedings: 4 }));
it('exportUsesDotDecimals', () => expect(toConfigJson({ p1: 0.25, r: 0.8, maxBreedings: null }, DEFAULT_CURVE)).toContain('"p1": 0.25'));
