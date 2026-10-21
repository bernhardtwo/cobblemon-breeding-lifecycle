// Builds the page, serves it with `vite preview` and saves 24 full-page screenshots into docs/screenshots/.
import { execFileSync, spawn } from 'node:child_process';
import { createServer } from 'node:net';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromium } from 'playwright';

const webDir = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const outDir = resolve(webDir, '../docs/screenshots');
const vite = resolve(webDir, 'node_modules/.bin/vite');

const WIDTHS = [390, 1440];
const THEMES = ['light', 'dark'];
const LANGS = ['es', 'en'];
const STATES = ['initial', 'sliders', 'exported'];

function freePort() {
  return new Promise((done, fail) => {
    const server = createServer();
    server.once('error', fail);
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      server.close(() => done(port));
    });
  });
}

async function waitFor(url) {
  for (let i = 0; i < 100; i++) {
    try {
      if ((await fetch(url)).ok) return;
    } catch {
      // Not listening yet.
    }
    await new Promise((r) => setTimeout(r, 100));
  }
  throw new Error(`vite preview did not answer at ${url}`);
}

async function setValue(page, testId, value) {
  await page.locator(`[data-testid=${testId}]`).fill(value);
}

execFileSync('npm', ['run', 'build'], { cwd: webDir, stdio: 'inherit' });
const port = await freePort();
const preview = spawn(vite, ['preview', '--port', String(port), '--strictPort', '--host', '127.0.0.1'], { cwd: webDir, stdio: 'ignore' });
const base = `http://127.0.0.1:${port}/`;
const overflows = [];
let browser;
try {
  await waitFor(base);
  browser = await chromium.launch();
  for (const width of WIDTHS) {
    for (const theme of THEMES) {
      for (const lang of LANGS) {
        for (const state of STATES) {
          const context = await browser.newContext({ viewport: { width, height: 900 }, colorScheme: theme, locale: lang === 'es' ? 'es-ES' : 'en-US' });
          const page = await context.newPage();
          await page.goto(`${base}?lang=${lang}`);
          await page.evaluate(() => document.fonts.ready);
          if (state === 'sliders') {
            await setValue(page, 'slider-p1', '0.5');
            await setValue(page, 'slider-r', '0.9');
          }
          if (state === 'exported') {
            await page.click('[data-testid=export]');
          }
          const [scrollWidth, innerWidth] = await page.evaluate(() => [document.documentElement.scrollWidth, window.innerWidth]);
          const name = `web-${width}-${theme}-${lang}-${state}.png`;
          if (scrollWidth > innerWidth) overflows.push(`${name}: scrollWidth ${scrollWidth} > ${innerWidth}`);
          await page.screenshot({ path: resolve(outDir, name), fullPage: true });
          console.log(name);
          await context.close();
        }
      }
    }
  }
} finally {
  await browser?.close();
  preview.kill();
}
if (overflows.length) {
  console.error(`Horizontal scroll:\n${overflows.join('\n')}`);
  process.exit(1);
}
