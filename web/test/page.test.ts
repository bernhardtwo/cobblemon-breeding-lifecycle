// @vitest-environment jsdom
import { beforeEach, expect, it } from 'vitest';
import { formatDecimal } from '../src/i18n';
import { mount } from '../src/main';

let root: HTMLElement;

beforeEach(() => {
  document.body.innerHTML = '<div id="test-root"></div>';
  root = document.getElementById('test-root')!;
});

const env = (search: string) => ({ search, navigatorLanguage: 'en-US', prefersDark: true, storage: null });

function setSlider(name: string, value: string): void {
  const input = root.querySelector<HTMLInputElement>(`[data-testid=slider-${name}]`)!;
  input.value = value;
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

function click(selector: string): void {
  root.querySelector<HTMLElement>(selector)!.click();
}

const text = (testId: string) => root.querySelector(`[data-testid=${testId}]`)!.textContent;

it('initialPageShowsBaselineE', () => {
  mount(root, env('?lang=en'));
  expect(text('e-value')).toBe('2.20');
  expect(text('e-current')).toBe('1.00');
});

it('movingASliderUpdatesE', () => {
  mount(root, env('?lang=en'));
  setSlider('r', '1');
  expect(text('e-value')).toBe(formatDecimal('en', 1 / 0.33, 2));
});

it('exportIsDisabledForInvalidCurve', () => {
  mount(root, env('?lang=en'));
  setSlider('p1', '0');
  setSlider('r', '1');
  expect(root.querySelector<HTMLButtonElement>('[data-testid=export]')!.disabled).toBe(true);
  expect(root.textContent).toContain('set maxBreedings');
});

it('langToggleSwitchesText', () => {
  mount(root, env('?lang=en'));
  click('[data-testid=lang-es]');
  expect(text('e-value')).toBe('2,20');
});

it('dittoSlidersAreIndependent', () => {
  mount(root, env('?lang=en'));
  setSlider('ditto-p1', '0.5');
  expect(text('e-value')).toBe('2.20');
});

it('maxBreedingsLimitsE', () => {
  mount(root, env('?lang=en'));
  click('[data-testid=nolimit-main]');
  setSlider('max-main', '3');
  expect(text('e-value')).toBe('2.01');
});

it('exportPanelShowsTheConfig', () => {
  mount(root, env('?lang=en'));
  click('[data-testid=copy]');
  expect(JSON.parse(text('export-json')!).presets.default.maxBreedings).toBeNull();
});

it('themeToggleSetsDocumentTheme', () => {
  mount(root, env('?lang=en'));
  click('[data-testid=theme-light]');
  expect(document.documentElement.dataset.theme).toBe('light');
});
