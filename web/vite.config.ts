import { defineConfig } from 'vite';

export default defineConfig({
  base: process.env.WEB_BASE ?? '/',
});
