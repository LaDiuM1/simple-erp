import { defineConfig, devices } from '@playwright/test';

const liveBaseURL = process.env.DEMO_E2E_BASE_URL;

export default defineConfig({
  testDir: './e2e',
  testIgnore: liveBaseURL ? /demo-experience\.spec\.ts/ : /demo-live\.spec\.ts/,
  fullyParallel: !liveBaseURL,
  workers: liveBaseURL ? 1 : undefined,
  retries: process.env.CI ? 2 : 0,
  reporter: 'list',
  use: {
    baseURL: liveBaseURL ?? 'http://127.0.0.1:4173',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: liveBaseURL
    ? undefined
    : {
        command: 'npm run preview -- --host 127.0.0.1 --port 4173',
        url: 'http://127.0.0.1:4173',
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
      },
});
