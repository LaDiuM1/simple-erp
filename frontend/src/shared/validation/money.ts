/** BE MoneyPolicy.MAX_AMOUNT와 동일한 JSON Number 안전 금액 상한. */
export const MAX_MONEY_AMOUNT = 9_999_999_999_999;

const AMOUNT_RE = /^\d+$/;

export function validateMoneyAmount(value: string, required = false): string | null {
  const amount = value.trim();
  if (amount === '') return required ? '금액을 입력해주세요.' : null;
  if (!AMOUNT_RE.test(amount)) return '금액은 숫자만 입력해주세요.';
  const parsed = Number(amount);
  if (!Number.isSafeInteger(parsed) || parsed > MAX_MONEY_AMOUNT) {
    return `금액은 ${MAX_MONEY_AMOUNT.toLocaleString('ko-KR')}원 이하로 입력해주세요.`;
  }
  return null;
}

export function hasCompleteDateAmountPair(date: string, amount: string): boolean {
  return (date.trim() === '') === (amount.trim() === '');
}
