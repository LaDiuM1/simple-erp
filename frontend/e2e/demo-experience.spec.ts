import { expect, test } from '@playwright/test';

test('데모 계정 quick-fill과 reset 중 전체 route 차단', async ({ page }) => {
  let state: 'READY' | 'RESETTING' | 'VERIFYING' = 'READY';

  await page.route('**/api/v1/demo/status', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      headers: { 'cache-control': 'no-store' },
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          enabled: true,
          environmentName: 'DEMO',
          state,
          stateChangedAt: '2099-08-02T06:00:00.000Z',
          generation: 'generation-a',
          candidateGeneration: state === 'READY' ? null : 'generation-b',
          lastResetAt: '2099-08-02T06:00:00.000Z',
          nextResetAt: '2099-08-02T12:00:00.000Z',
          warningBeforeSeconds: 300,
          writeLockBeforeSeconds: 120,
          writeLocked: state !== 'READY',
          notice: '모든 데이터는 합성 데이터입니다.',
          uploadEnabled: true,
          simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
          publicAccounts: [{
            label: '관리자',
            description: '전체 데모 흐름',
            loginId: 'demo.manager',
            password: 'public-password',
            recommended: true,
          }],
        },
      }),
    });
  });

  await page.goto('/login');
  await expect(page.getByText('모든 정보는 합성 데이터이며 주기적으로 초기화됩니다.')).toBeVisible();

  await page.getByRole('button', { name: /관리자.*추천/ }).click();
  await expect(page.getByLabel('아이디')).toHaveValue('demo.manager');
  await expect(page.getByLabel('비밀번호')).toHaveValue('public-password');
  await expect(page).toHaveURL(/\/login$/);

  state = 'RESETTING';
  await page.reload();
  await expect(page.getByText('데모 데이터를 초기화하고 있어요')).toBeVisible();
  await expect(page.getByRole('button', { name: '로그인' })).toHaveCount(0);

  state = 'VERIFYING';
  await page.goto('/boards/new');
  await expect(page.getByText('데모 데이터를 초기화하고 있어요')).toBeVisible();
  await expect(page.getByRole('button', { name: '파일 선택' })).toHaveCount(0);
});
