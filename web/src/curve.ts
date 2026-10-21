/** Loss curve of one parent; `maxBreedings` is the breeding at which it is always lost, or null. */
export type Curve = { p1: number; r: number; maxBreedings: number | null };

const SURVIVAL_EPSILON = 1e-12;
const MAX_UNLIMITED_BREEDINGS = 1_000_000;

/** The `default` preset curve: `p1 = 0.33`, `r = 0.764`, no limit. */
export const DEFAULT_CURVE: Curve = { p1: 0.33, r: 0.764, maxBreedings: null };

/** Returns one message per invalid field, with the same texts as `Curve.validate` in `core`; empty when valid. */
export function validateCurve(c: Curve): string[] {
  const errors: string[] = [];
  if (!(c.p1 >= 0 && c.p1 <= 1)) {
    errors.push(`p1 = ${c.p1}: must be in [0, 1]`);
  }
  if (!(c.r > 0 && c.r <= 1)) {
    errors.push(`r = ${c.r}: must be in (0, 1]`);
  }
  if (c.maxBreedings !== null && !(Number.isInteger(c.maxBreedings) && c.maxBreedings >= 1)) {
    errors.push(`maxBreedings = ${c.maxBreedings}: must be null or an integer >= 1`);
  }
  if (errors.length === 0 && c.maxBreedings === null) {
    if (c.p1 === 0 && c.r === 1) {
      errors.push('p1 = 0 and r = 1: a parent would never be lost; set maxBreedings');
    } else if (!survivalVanishes(c.p1, c.r)) {
      errors.push(
        `p1 = ${c.p1} and r = ${c.r}: a parent would practically never be lost` +
          ` (still there after ${MAX_UNLIMITED_BREEDINGS} breedings); set maxBreedings`,
      );
    }
  }
  return errors;
}

/** Probability of losing the parent in its `n`-th breeding, `n >= 1`. */
export function lossProbability(c: Curve, n: number): number {
  requirePositive(n);
  if (c.maxBreedings !== null && n >= c.maxBreedings) {
    return 1;
  }
  return 1 - (1 - c.p1) * Math.pow(c.r, n - 1);
}

/** Probability that the parent is still there for its `n`-th breeding. */
export function survival(c: Curve, n: number): number {
  requirePositive(n);
  let s = 1;
  for (let k = 1; k < n; k++) {
    s *= 1 - lossProbability(c, k);
  }
  return s;
}

/** Expected number of offspring per parent, the sum of `survival(n)`. */
export function expectedOffspring(c: Curve): number {
  let expected = 0;
  let s = 1;
  for (let n = 1; s >= SURVIVAL_EPSILON && (c.maxBreedings === null || n <= c.maxBreedings); n++) {
    expected += s;
    s *= 1 - lossProbability(c, n);
  }
  return expected;
}

function survivalVanishes(p1: number, r: number): boolean {
  let s = 1;
  for (let n = 1; n <= MAX_UNLIMITED_BREEDINGS; n++) {
    s *= (1 - p1) * Math.pow(r, n - 1);
    if (s < SURVIVAL_EPSILON) {
      return true;
    }
  }
  return false;
}

function requirePositive(n: number): void {
  if (!(Number.isInteger(n) && n >= 1)) {
    throw new RangeError(`n = ${n}: must be >= 1`);
  }
}
