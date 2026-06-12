import { defineConfig } from "vite";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    VitePWA({
      devOptions: {
        enabled: true, // Enable SW in development
        type: "module",
      },
      injectRegister: "auto",
      registerType: "prompt",
      includeAssets: ["favicon.ico", "quran.sqlite", "sql-wasm-browser.wasm"],
      workbox: {
        cleanupOutdatedCaches: true,
        additionalManifestEntries: [
          {
            url: "https://cdnjs.cloudflare.com/ajax/libs/bootswatch/5.3.8/flatly/bootstrap.rtl.min.css",
            revision: null,
          },
          {
            url: "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.13.1/font/bootstrap-icons.min.css",
            revision: null,
          },
          {
            url: "https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.8/js/bootstrap.bundle.min.js",
            revision: null,
          },
        ],
        runtimeCaching: [
          {
            urlPattern: ({ url }) =>
              url.origin === "https://fonts.googleapis.com" ||
              url.origin === "https://fonts.gstatic.com" ||
              url.origin === "https://cdnjs.cloudflare.com",
            handler: "CacheFirst",
            options: {
              cacheName: "core-cache-first",
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 * 30, // 30 days
              },
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
        ],
      },
    }),
  ],
});
