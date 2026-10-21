import { expect, it } from 'vitest';
import { linePath } from '../src/chart';

it('linePathStartsAtTopLeftForOne', () => expect(linePath([1, 0], 100, 50).startsWith('M0,0')).toBe(true));
it('linePathEndsAtBottomRightForZero', () => expect(linePath([1, 0], 100, 50)).toBe('M0,0 L100,50'));
it('linePathPlacesMiddleValueHalfway', () => expect(linePath([0, 0.5, 1], 100, 50)).toBe('M0,50 L50,25 L100,0'));
