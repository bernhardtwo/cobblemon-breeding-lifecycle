import type { Curve } from './curve';

/** Returns a `breeding-lifecycle.json` with a `default` preset made of these curves, in the key order of `DefaultConfig`. */
export function toConfigJson(main: Curve, ditto: Curve): string {
  const config = {
    preset: 'default',
    presets: {
      default: {
        p1: main.p1,
        r: main.r,
        maxBreedings: main.maxBreedings,
        ditto: { p1: ditto.p1, r: ditto.r, maxBreedings: ditto.maxBreedings },
      },
    },
    overrides: {},
    referenceDaycare: { enabled: false },
    eggEventAdapter: { enabled: false },
    audit: { enabled: true },
  };
  return `${JSON.stringify(config, null, 2)}\n`;
}
