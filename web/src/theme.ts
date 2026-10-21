export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'breeding-lifecycle-theme';

/** The stored theme if it is `light` or `dark`; otherwise, or when storage throws, the system preference. */
export function resolveTheme(storage: Pick<Storage, 'getItem'> | null, prefersDark: boolean): Theme {
  try {
    const stored = storage?.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
  } catch {
    // Private mode or blocked storage: follow the system.
  }
  return prefersDark ? 'dark' : 'light';
}

/** Remembers the chosen theme; a storage that throws is ignored. */
export function storeTheme(storage: Pick<Storage, 'setItem'> | null, theme: Theme): void {
  try {
    storage?.setItem(STORAGE_KEY, theme);
  } catch {
    // Private mode or full storage: the choice lasts for this page only.
  }
}
