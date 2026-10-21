export type Lang = 'es' | 'en';

const en = {
  title: 'Breeding lifecycle simulator',
  intro: 'Each breeding can cost each parent, with a chance that grows with every breeding. Move the curve and compare it with the current system of one offspring per parent.',
  mainCurve: 'Main curve',
  dittoCurve: 'Ditto curve',
  p1Label: 'p1: chance of losing the parent in its first breeding',
  rLabel: 'r: how much of the remaining chance is kept on each breeding',
  maxLabel: 'maxBreedings: the breeding at which the parent is always lost',
  noLimit: 'No limit',
  expectedLabel: 'Expected offspring per parent (E)',
  currentLabel: 'Current system',
  currentNote: 'one offspring per parent',
  invalidHeading: 'Invalid curve:',
  notComputed: '—',
  lossChart: 'Loss probability p(n)',
  survivalChart: 'Survival S(n)',
  axisBreeding: 'breeding n',
  versus: 'vs',
  tableCaption: 'p(n) and S(n) for the first breedings',
  exportHeading: 'Config',
  exportHint: 'A breeding-lifecycle.json with these curves as the default preset, valid for the mod and the CLI.',
  exportButton: 'Export JSON',
  copyButton: 'Copy',
  copied: 'Copied',
  copyFailed: 'Could not copy; select the text below.',
  exportDisabled: 'Fix the invalid curve to export.',
  languageLabel: 'Language',
  themeLabel: 'Theme',
  themeLight: 'Light',
  themeDark: 'Dark',
  footer: 'Exact calculation in your browser, checked against the same test vectors as the Java core.',
};

export type MessageKey = keyof typeof en;

const es: Record<MessageKey, string> = {
  title: 'Simulador del ciclo de crianza',
  intro: 'Cada crianza puede costarle cada padre, con una probabilidad que crece en cada crianza. Mueve la curva y compárala con el sistema actual de una cría por padre.',
  mainCurve: 'Curva principal',
  dittoCurve: 'Curva de Ditto',
  p1Label: 'p1: probabilidad de perder al padre en su primera crianza',
  rLabel: 'r: cuánto de la probabilidad restante se conserva en cada crianza',
  maxLabel: 'maxBreedings: la crianza en la que el padre se pierde siempre',
  noLimit: 'Sin límite',
  expectedLabel: 'Crías esperadas por padre (E)',
  currentLabel: 'Sistema actual',
  currentNote: 'una cría por padre',
  invalidHeading: 'Curva inválida:',
  notComputed: '—',
  lossChart: 'Probabilidad de pérdida p(n)',
  survivalChart: 'Supervivencia S(n)',
  axisBreeding: 'crianza n',
  versus: 'frente a',
  tableCaption: 'p(n) y S(n) en las primeras crianzas',
  exportHeading: 'Config',
  exportHint: 'Un breeding-lifecycle.json con estas curvas como preset default, válido para el mod y el CLI.',
  exportButton: 'Exportar JSON',
  copyButton: 'Copiar',
  copied: 'Copiado',
  copyFailed: 'No se pudo copiar; selecciona el texto de abajo.',
  exportDisabled: 'Corrige la curva inválida para exportar.',
  languageLabel: 'Idioma',
  themeLabel: 'Tema',
  themeLight: 'Claro',
  themeDark: 'Oscuro',
  footer: 'Cálculo exacto en tu navegador, comprobado con los mismos vectores de prueba que el core en Java.',
};

/** Both dictionaries, with the same keys. */
export const messages: Record<Lang, Record<MessageKey, string>> = { es, en };

/** `?lang=es|en` wins, in any case; otherwise Spanish if the browser language starts with `es`, else English. */
export function resolveLang(search: string, navigatorLanguage: string | undefined): Lang {
  const param = new URLSearchParams(search).get('lang')?.trim().toLowerCase();
  if (param === 'es' || param === 'en') {
    return param;
  }
  return navigatorLanguage?.toLowerCase().startsWith('es') ? 'es' : 'en';
}

/** Returns the message for `key` in `lang`. */
export function t(lang: Lang, key: MessageKey): string {
  return messages[lang][key];
}

/** Formats `value` with exactly `digits` decimals: a comma in Spanish, a dot in English, no grouping. */
export function formatDecimal(lang: Lang, value: number, digits: number): string {
  return new Intl.NumberFormat(lang, {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
    useGrouping: false,
  }).format(value);
}
