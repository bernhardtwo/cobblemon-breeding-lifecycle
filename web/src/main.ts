import '@fontsource/funnel-display/600.css';
import '@fontsource/funnel-display/700.css';
import '@fontsource/funnel-sans/400.css';
import '@fontsource/funnel-sans/500.css';
import '@fontsource/archivo/400.css';
import '@fontsource/archivo/500.css';
import '@fontsource/archivo/600.css';
import './styles.css';
import { drawChart } from './chart';
import { toConfigJson } from './config';
import { type Curve, DEFAULT_CURVE, expectedOffspring, lossProbability, survival, validateCurve } from './curve';
import { formatDecimal, type Lang, type MessageKey, resolveLang, t } from './i18n';
import { resolveTheme, storeTheme, type Theme } from './theme';

export type PageEnv = { search: string; navigatorLanguage?: string; prefersDark: boolean; storage: Storage | null };

type CurveId = 'main' | 'ditto';

const CHART_BREEDINGS = 12;
const TABLE_BREEDINGS = 8;
const DEFAULT_LIMIT = 5;
const FILE_NAME = 'breeding-lifecycle.json';

/** Builds the simulator page inside `root`, reading language and theme from `env`. */
export function mount(root: HTMLElement, env: PageEnv): void {
  const doc = root.ownerDocument;
  const state = {
    lang: resolveLang(env.search, env.navigatorLanguage),
    theme: resolveTheme(env.storage, env.prefersDark),
    curves: { main: { ...DEFAULT_CURVE }, ditto: { ...DEFAULT_CURVE } } as Record<CurveId, Curve>,
    exportShown: false,
  };

  root.innerHTML = pageHtml();
  const q = <T extends Element = HTMLElement>(testId: string): T => root.querySelector<T>(`[data-testid="${testId}"]`)!;

  const fmt = (value: number, digits: number) => formatDecimal(state.lang, value, digits);

  function applyTheme(theme: Theme): void {
    state.theme = theme;
    doc.documentElement.dataset.theme = theme;
    for (const option of ['light', 'dark'] as const) {
      q(`theme-${option}`).setAttribute('aria-pressed', String(option === theme));
    }
  }

  function applyLang(lang: Lang): void {
    state.lang = lang;
    doc.documentElement.lang = lang;
    root.querySelectorAll<HTMLElement>('[data-i18n]').forEach((el) => {
      el.textContent = t(lang, el.dataset.i18n as MessageKey);
    });
    for (const option of ['es', 'en'] as const) {
      q(`lang-${option}`).setAttribute('aria-pressed', String(option === lang));
    }
    q('copy-status').textContent = '';
    update();
  }

  function readCurve(id: CurveId): Curve {
    const noLimit = q<HTMLInputElement>(`nolimit-${id}`).checked;
    const max = q<HTMLInputElement>(`slider-max-${id}`);
    max.disabled = noLimit;
    return {
      p1: Number(q<HTMLInputElement>(`slider-${prefix(id)}p1`).value),
      r: Number(q<HTMLInputElement>(`slider-${prefix(id)}r`).value),
      maxBreedings: noLimit ? null : Number(max.value),
    };
  }

  function update(): void {
    let allValid = true;
    for (const id of ['main', 'ditto'] as const) {
      const curve = (state.curves[id] = readCurve(id));
      const errors = validateCurve(curve);
      allValid &&= errors.length === 0;
      q(`out-${prefix(id)}p1`).textContent = fmt(curve.p1, 2);
      q(`out-${prefix(id)}r`).textContent = fmt(curve.r, 3);
      q(id === 'main' ? 'e-value' : 'e-ditto').textContent = errors.length ? t(state.lang, 'notComputed') : fmt(expectedOffspring(curve), 2);
      const box = q(`errors-${id}`);
      box.hidden = errors.length === 0;
      box.replaceChildren();
      if (errors.length) {
        const heading = doc.createElement('strong');
        heading.textContent = t(state.lang, 'invalidHeading');
        const list = doc.createElement('ul');
        for (const error of errors) {
          const item = doc.createElement('li');
          item.textContent = error;
          list.appendChild(item);
        }
        box.append(heading, list);
      }
    }
    q('e-current').textContent = fmt(1, 2);

    const exportButton = q<HTMLButtonElement>('export');
    const copyButton = q<HTMLButtonElement>('copy');
    exportButton.disabled = copyButton.disabled = !allValid;
    q('export-disabled').hidden = allValid;
    const json = q('export-json');
    json.textContent = allValid ? toConfigJson(state.curves.main, state.curves.ditto) : '';
    q('export-panel').hidden = !(state.exportShown && allValid);

    drawCharts();
    fillTable();
  }

  function drawCharts(): void {
    const ns = Array.from({ length: CHART_BREEDINGS }, (_, i) => i + 1);
    const text = (title: MessageKey) => ({ title: t(state.lang, title), axisLabel: t(state.lang, 'axisBreeding'), format: (v: number) => fmt(v, 2) });
    const series = (f: (c: Curve, n: number) => number) =>
      (['main', 'ditto'] as const).map((id) => ({
        label: t(state.lang, id === 'main' ? 'mainCurve' : 'dittoCurve'),
        values: ns.map((n) => f(state.curves[id], n)),
        className: `series-${id}`,
      }));
    drawChart(q<SVGSVGElement>('chart-loss'), series(lossProbability), text('lossChart'));
    drawChart(q<SVGSVGElement>('chart-survival'), series(survival), text('survivalChart'));
  }

  function fillTable(): void {
    const body = q('table-body');
    body.replaceChildren();
    const { main, ditto } = state.curves;
    for (let n = 1; n <= TABLE_BREEDINGS; n++) {
      const row = doc.createElement('tr');
      const cells = [String(n), ...[lossProbability(main, n), survival(main, n), lossProbability(ditto, n), survival(ditto, n)].map((v) => fmt(v, 4))];
      cells.forEach((value, i) => {
        const cell = doc.createElement(i === 0 ? 'th' : 'td');
        if (i === 0) cell.setAttribute('scope', 'row');
        cell.textContent = value;
        row.appendChild(cell);
      });
      body.appendChild(row);
    }
  }

  function showExport(): void {
    state.exportShown = true;
    update();
  }

  root.addEventListener('input', (event) => {
    if ((event.target as HTMLElement).closest('[data-curve]')) update();
  });
  root.addEventListener('change', (event) => {
    if ((event.target as HTMLElement).closest('[data-curve]')) update();
  });
  q('lang-es').addEventListener('click', () => applyLang('es'));
  q('lang-en').addEventListener('click', () => applyLang('en'));
  for (const option of ['light', 'dark'] as const) {
    q(`theme-${option}`).addEventListener('click', () => {
      applyTheme(option);
      storeTheme(env.storage, option);
    });
  }
  q('export').addEventListener('click', () => {
    showExport();
    download(doc, q('export-json').textContent ?? '');
  });
  q('copy').addEventListener('click', () => {
    showExport();
    const status = q('copy-status');
    const clipboard = doc.defaultView?.navigator.clipboard;
    if (!clipboard) {
      status.textContent = t(state.lang, 'copyFailed');
      return;
    }
    clipboard.writeText(q('export-json').textContent ?? '').then(
      () => (status.textContent = t(state.lang, 'copied')),
      () => (status.textContent = t(state.lang, 'copyFailed')),
    );
  });

  applyTheme(state.theme);
  applyLang(state.lang);
}

function download(doc: Document, json: string): void {
  const url = URL.createObjectURL(new Blob([json], { type: 'application/json' }));
  const link = doc.createElement('a');
  link.href = url;
  link.download = FILE_NAME;
  doc.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 0);
}

function prefix(id: CurveId): string {
  return id === 'main' ? '' : 'ditto-';
}

function curveFieldset(id: CurveId): string {
  const p = prefix(id);
  const legend: MessageKey = id === 'main' ? 'mainCurve' : 'dittoCurve';
  const v = DEFAULT_CURVE;
  return `
    <fieldset class="card curve curve-${id}" data-curve="${id}">
      <legend><span class="swatch swatch-${id}" aria-hidden="true"></span><span data-i18n="${legend}"></span></legend>
      <div class="field">
        <label for="bl-${p}p1" data-i18n="p1Label"></label>
        <div class="range-row">
          <input id="bl-${p}p1" data-testid="slider-${p}p1" type="range" min="0" max="1" step="0.01" value="${v.p1}" />
          <output for="bl-${p}p1" data-testid="out-${p}p1"></output>
        </div>
      </div>
      <div class="field">
        <label for="bl-${p}r" data-i18n="rLabel"></label>
        <div class="range-row">
          <input id="bl-${p}r" data-testid="slider-${p}r" type="range" min="0.01" max="1" step="0.001" value="${v.r}" />
          <output for="bl-${p}r" data-testid="out-${p}r"></output>
        </div>
      </div>
      <div class="field">
        <label for="bl-max-${id}" data-i18n="maxLabel"></label>
        <div class="limit-row">
          <label class="check"><input id="bl-nolimit-${id}" data-testid="nolimit-${id}" type="checkbox" checked /><span data-i18n="noLimit"></span></label>
          <input id="bl-max-${id}" data-testid="slider-max-${id}" type="number" min="1" max="20" step="1" value="${DEFAULT_LIMIT}" inputmode="numeric" disabled />
        </div>
      </div>
      <div class="errors" role="alert" data-testid="errors-${id}" hidden></div>
    </fieldset>`;
}

function pageHtml(): string {
  return `
  <div class="page">
    <header class="top">
      <div class="brand">
        <p class="eyebrow">Cobblemon · Breeding Lifecycle</p>
        <h1 data-i18n="title"></h1>
        <p class="intro" data-i18n="intro"></p>
      </div>
      <div class="toggles">
        <div class="toggle" role="group" aria-labelledby="bl-lang-label">
          <span id="bl-lang-label" class="label" data-i18n="languageLabel"></span>
          <button type="button" data-testid="lang-es" lang="es">ES</button>
          <button type="button" data-testid="lang-en" lang="en">EN</button>
        </div>
        <div class="toggle" role="group" aria-labelledby="bl-theme-label">
          <span id="bl-theme-label" class="label" data-i18n="themeLabel"></span>
          <button type="button" data-testid="theme-light" data-i18n="themeLight"></button>
          <button type="button" data-testid="theme-dark" data-i18n="themeDark"></button>
        </div>
      </div>
    </header>

    <main>
      <section class="summary card" aria-live="polite">
        <div class="metric">
          <span class="label" data-i18n="expectedLabel"></span>
          <span class="big" data-testid="e-value"></span>
          <span class="note"><span class="swatch swatch-main" aria-hidden="true"></span><span data-i18n="mainCurve"></span></span>
        </div>
        <span class="versus" data-i18n="versus"></span>
        <div class="metric">
          <span class="label" data-i18n="currentLabel"></span>
          <span class="big muted-big" data-testid="e-current"></span>
          <span class="note" data-i18n="currentNote"></span>
        </div>
        <div class="metric">
          <span class="label" data-i18n="expectedLabel"></span>
          <span class="big ditto-big" data-testid="e-ditto"></span>
          <span class="note"><span class="swatch swatch-ditto" aria-hidden="true"></span><span data-i18n="dittoCurve"></span></span>
        </div>
      </section>

      <div class="curves">${curveFieldset('main')}${curveFieldset('ditto')}</div>

      <div class="charts">
        <figure class="card">
          <figcaption data-i18n="lossChart"></figcaption>
          <svg data-testid="chart-loss"></svg>
          ${legendHtml()}
        </figure>
        <figure class="card">
          <figcaption data-i18n="survivalChart"></figcaption>
          <svg data-testid="chart-survival"></svg>
          ${legendHtml()}
        </figure>
      </div>

      <section class="card table-card">
        <div class="table-scroll">
          <table>
            <caption data-i18n="tableCaption"></caption>
            <thead>
              <tr><th scope="col">n</th><th scope="col">p(n)</th><th scope="col">S(n)</th><th scope="col">p(n) Ditto</th><th scope="col">S(n) Ditto</th></tr>
            </thead>
            <tbody data-testid="table-body"></tbody>
          </table>
        </div>
      </section>

      <section class="card export" aria-labelledby="bl-export-heading">
        <h2 id="bl-export-heading" data-i18n="exportHeading"></h2>
        <p class="note" data-i18n="exportHint"></p>
        <div class="actions">
          <button type="button" class="btn" data-testid="export" data-i18n="exportButton"></button>
          <button type="button" class="btn-ghost" data-testid="copy" data-i18n="copyButton"></button>
          <span class="status" role="status" data-testid="copy-status"></span>
        </div>
        <p class="note warn" data-testid="export-disabled" data-i18n="exportDisabled" hidden></p>
        <div data-testid="export-panel" hidden>
          <p class="file">${FILE_NAME}</p>
          <pre tabindex="0"><code data-testid="export-json"></code></pre>
        </div>
      </section>
    </main>

    <footer class="note" data-i18n="footer"></footer>
  </div>`;
}

function legendHtml(): string {
  return `<ul class="legend">
    <li><span class="swatch swatch-main" aria-hidden="true"></span><span data-i18n="mainCurve"></span></li>
    <li><span class="swatch swatch-ditto dashed" aria-hidden="true"></span><span data-i18n="dittoCurve"></span></li>
  </ul>`;
}

const app = typeof document !== 'undefined' ? document.getElementById('app') : null;
if (app) {
  let storage: Storage | null = null;
  try {
    storage = window.localStorage;
  } catch {
    storage = null;
  }
  mount(app, {
    search: window.location.search,
    navigatorLanguage: navigator.language,
    prefersDark: window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? true,
    storage,
  });
}
