import { expect, test } from '@playwright/test';

const LIVE_DATA_TIMEOUT_MS = 30_000;

test.describe('실제 데모 종단 흐름', () => {
  test.skip(!process.env.DEMO_E2E_BASE_URL, 'DEMO_E2E_BASE_URL을 지정한 배포 전 검증에서만 실행');

  test('quick-fill 로그인 뒤 실제 업무 API가 연결된 화면을 연다', async ({ page }) => {
    test.setTimeout(90_000);
    await page.goto('/login');

    await expect(page.getByLabel('데모 계정')).toBeVisible();
    await expect(page.getByText('모든 정보는 합성 데이터이며 주기적으로 초기화됩니다.')).toBeVisible();

    await page.getByRole('button', { name: /관리자형 계정.*추천/ }).click();
    await expect(page.getByLabel('아이디')).toHaveValue('demo.manager');
    await expect(page.getByLabel('비밀번호')).toHaveValue('ManagerDemo!2026');
    await page.getByRole('button', { name: '로그인' }).click();

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByRole('status').getByText('데모', { exact: true })).toBeVisible();
    await expect(page.getByRole('link', { name: '고객사 관리' })).toBeVisible();

    await page.getByRole('link', { name: '고객사 관리' }).click();
    await expect(page).toHaveURL(/\/customers$/);
    await expect(page.getByText('총 48건')).toBeVisible({ timeout: LIVE_DATA_TIMEOUT_MS });
    await expect(page.getByRole('button', { name: '엑셀 업로드' })).toBeEnabled();

    await page.goto('/sales-contacts');
    await expect(page.getByText('총 72건')).toBeVisible({ timeout: LIVE_DATA_TIMEOUT_MS });
    await expect(page.getByRole('button', { name: '엑셀 업로드' })).toBeEnabled();

    await page.goto('/drive');
    await expect(page.getByRole('button', {
      name: '파일 업로드 · 파일당 최대 30.0MB',
    })).toBeEnabled({
      timeout: LIVE_DATA_TIMEOUT_MS,
    });

    await page.goto('/boards/new');
    await expect(page.getByRole('button', { name: '파일 선택' })).toBeEnabled();
    await expect(page.getByText(/합성 파일만 업로드/)).toBeVisible();

    await page.goto('/approvals/new');
    await expect(page.getByRole('button', { name: '파일 선택' })).toBeEnabled();
    await expect(page.getByText(/합성 파일만 업로드/)).toBeVisible();

    await page.goto('/expenses/new');
    await expect(page.getByRole('button', { name: '파일 선택' })).toBeEnabled();
    await expect(page.getByText(/합성 파일만 업로드/)).toBeVisible();

    await page.goto('/contracts');
    await expect(page.getByText('총 42건')).toBeVisible({ timeout: LIVE_DATA_TIMEOUT_MS });

    await page.goto('/after-services');
    await expect(page.getByText('총 45건')).toBeVisible({ timeout: LIVE_DATA_TIMEOUT_MS });

    await page.goto('/sales-customers/1');
    await page.getByRole('button', { name: '담당자 배정' }).click();
    await page.getByLabel('직원 검색').click();

    const employeeSearch = page.getByRole('dialog', { name: '직원 검색' });
    await expect(employeeSearch).toBeVisible();
    await expect(employeeSearch.getByRole('columnheader', { name: '이름' })).toBeVisible();
    await expect(employeeSearch.getByRole('columnheader', { name: '부서' })).toBeVisible();
    await expect(employeeSearch.getByRole('columnheader', { name: '직책' })).toBeVisible();
    await expect(employeeSearch.getByRole('radio', { name: '강하윤' })).toBeVisible();
    for (const hiddenHeader of ['로그인 ID', '권한', '이메일', '연락처', '입사일', '생년월일']) {
      await expect(employeeSearch.getByRole('columnheader', { name: hiddenHeader })).toHaveCount(0);
    }

    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/');
    await expect(page.getByRole('region', { name: '운영 흐름' })).toBeVisible({
      timeout: LIVE_DATA_TIMEOUT_MS,
    });
    await expect(page.getByLabel('수리 AS 현황').getByText(/원$/)).toBeVisible();
  });
});
