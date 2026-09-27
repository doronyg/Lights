import { defineConfig } from 'vite';

const appMode = process.env.VITE_APP_MODE || 'local';
console.log('[Vite] Building with APP_MODE:', appMode);

export default defineConfig({
  server: {
    port: 3000,
  },
  build: {
    outDir: 'www',
    emptyOutDir: true,
    target: 'es2015',
  },
  define: {
    __APP_MODE__: JSON.stringify(appMode),
  },
});


