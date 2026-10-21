import { expect, it } from 'vitest';
import { resolveTheme, storeTheme } from '../src/theme';

it('storedThemeWins', () => expect(resolveTheme({ getItem: () => 'light' }, true)).toBe('light'));
it('storageFailureFollowsSystem', () => {
  expect(resolveTheme({ getItem: () => { throw new Error('denied'); } }, true)).toBe('dark');
  expect(resolveTheme({ getItem: () => 'purple' }, false)).toBe('light');
  expect(resolveTheme(null, true)).toBe('dark');
});
it('storeThemeSwallowsStorageFailure', () => {
  const saved: string[] = [];
  storeTheme({ setItem: (_k, v) => { saved.push(v); } }, 'dark');
  expect(saved).toEqual(['dark']);
  expect(() => storeTheme({ setItem: () => { throw new Error('quota'); } }, 'light')).not.toThrow();
});
