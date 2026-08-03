interface CustomerContactSelection {
  customerContactId: string;
  customerContactSelectedName: string;
}

/** 고객 문맥이 바뀔 때 이전 고객사 담당자 참조만 제거하고 나머지 작성 내용은 보존한다. */
export function clearActivityCustomerContact<T extends CustomerContactSelection>(values: T): T {
  return {
    ...values,
    customerContactId: '',
    customerContactSelectedName: '',
  };
}
