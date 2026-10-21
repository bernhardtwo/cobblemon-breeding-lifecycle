const SVG_NS = 'http://www.w3.org/2000/svg';

/** SVG path `d` through `values` in [0, 1], spread evenly over `width`, with 1 at the top and 0 at the bottom. */
export function linePath(values: number[], width: number, height: number): string {
  const step = values.length > 1 ? width / (values.length - 1) : 0;
  return values
    .map((v, i) => `${i === 0 ? 'M' : 'L'}${round(i * step)},${round((1 - v) * height)}`)
    .join(' ');
}

export type Series = { label: string; values: number[]; className: string };

export type ChartText = { title: string; axisLabel: string; format: (value: number) => string };

const WIDTH = 480;
const HEIGHT = 260;
const PAD = { top: 16, right: 16, bottom: 44, left: 48 };
const Y_TICKS = [0, 0.25, 0.5, 0.75, 1];

/** Draws a line chart of `series` (values for n = 1..k) with axes and a legend into `svg`, replacing its contents. */
export function drawChart(svg: SVGSVGElement, series: Series[], text: ChartText): void {
  const plotW = WIDTH - PAD.left - PAD.right;
  const plotH = HEIGHT - PAD.top - PAD.bottom;
  const count = series[0]?.values.length ?? 0;
  svg.replaceChildren();
  svg.setAttribute('viewBox', `0 0 ${WIDTH} ${HEIGHT}`);
  svg.setAttribute('role', 'img');
  svg.setAttribute('aria-label', text.title);
  add(svg, 'title', {}).textContent = text.title;

  const plot = add(svg, 'g', { transform: `translate(${PAD.left},${PAD.top})` });
  for (const tick of Y_TICKS) {
    const y = round((1 - tick) * plotH);
    add(plot, 'line', { x1: 0, x2: plotW, y1: y, y2: y, class: tick === 0 ? 'axis' : 'grid' });
    const label = add(plot, 'text', { x: -8, y, class: 'tick', 'text-anchor': 'end', 'dominant-baseline': 'middle' });
    label.textContent = text.format(tick);
  }
  add(plot, 'line', { x1: 0, x2: 0, y1: 0, y2: plotH, class: 'axis' });
  const step = count > 1 ? plotW / (count - 1) : 0;
  for (let i = 0; i < count; i++) {
    const label = add(plot, 'text', { x: round(i * step), y: plotH + 18, class: 'tick', 'text-anchor': 'middle' });
    label.textContent = String(i + 1);
  }
  const axis = add(plot, 'text', { x: plotW / 2, y: plotH + 38, class: 'axis-label', 'text-anchor': 'middle' });
  axis.textContent = text.axisLabel;

  for (const s of series) {
    add(plot, 'path', { d: linePath(s.values, plotW, plotH), class: `series ${s.className}`, fill: 'none' });
    s.values.forEach((v, i) => add(plot, 'circle', { cx: round(i * step), cy: round((1 - v) * plotH), r: 3, class: `dot ${s.className}` }));
  }
}

function add(parent: Element, tag: string, attrs: Record<string, string | number>): SVGElement {
  const el = document.createElementNS(SVG_NS, tag);
  for (const [name, value] of Object.entries(attrs)) {
    el.setAttribute(name, String(value));
  }
  parent.appendChild(el);
  return el;
}

function round(value: number): number {
  return Math.round(value * 100) / 100;
}
