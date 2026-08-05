import { hasCompleteDateAmountPair, validateMoneyAmount } from '@/shared/validation/money';

export interface PaymentAmountValues {
  plannedAmount: string;
  paidDate: string;
  paidAmount: string;
  invoiceDate: string;
  invoiceAmount: string;
}

export function validatePaymentAmounts(values: PaymentAmountValues): string | null {
  const amountFields: Array<[string, string]> = [
    ['예정 금액', values.plannedAmount],
    ['입금액', values.paidAmount],
    ['세금계산서 금액', values.invoiceAmount],
  ];
  for (const [label, value] of amountFields) {
    const error = validateMoneyAmount(value);
    if (error) return `${label}: ${error}`;
  }
  if (!hasCompleteDateAmountPair(values.paidDate, values.paidAmount)) {
    return '입금일과 입금액을 함께 입력해주세요.';
  }
  if (!hasCompleteDateAmountPair(values.invoiceDate, values.invoiceAmount)) {
    return '세금계산서 발행일과 금액을 함께 입력해주세요.';
  }
  return null;
}
