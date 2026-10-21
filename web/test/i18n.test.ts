import { expect, it } from 'vitest';
import { formatDecimal, messages, resolveLang } from '../src/i18n';

it('paramWins', () => expect(resolveLang('?lang=en', 'es-MX')).toBe('en'));
it('langParamIsCaseInsensitive', () => expect(resolveLang('?lang=ES', 'en-US')).toBe('es'));
it('unknownLangParamFallsBack', () => expect(resolveLang('?lang=fr', 'es-AR')).toBe('es'));
it('emptyNavigatorIsEnglish', () => expect(resolveLang('', undefined)).toBe('en'));
it('dictionariesHaveSameKeys', () => expect(Object.keys(messages.es).sort()).toEqual(Object.keys(messages.en).sort()));
it('formatsDecimalsPerLanguage', () => {
  expect(formatDecimal('es', 2.1981, 2)).toBe('2,20');
  expect(formatDecimal('en', 2.1981, 2)).toBe('2.20');
});
