import type { SupplierInfo } from '@/features/reference/types';

export function eligibleSupplierOptions(
  suppliers: SupplierInfo[],
  currentSupplierId: number | null,
): SupplierInfo[] {
  return suppliers.filter((supplier) => supplier.active || supplier.id === currentSupplierId);
}
