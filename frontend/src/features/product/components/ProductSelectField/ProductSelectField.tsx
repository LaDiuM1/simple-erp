import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetProductsSummaryQuery } from '@/features/product/api/productApi';
import {
  productListFilters,
  productSelectColumns,
} from '@/features/product/config/productListConfig';
import type { ProductSummary } from '@/features/product/types';

interface Props {
  label?: string;
  /** 제품 모델 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유 (form values 가 productId + productModelName 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
}

const productSelectConfig: EntitySelectConfig<ProductSummary> = {
  modalTitle: '제품 모델 검색',
  searchAriaLabel: '제품 모델 검색',
  useSearchList: useGetProductsSummaryQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.modelName,
  searchFilter: productListFilters,
  column: productSelectColumns,
};

/** 제품 모델 검색 SelectField — CustomerSelectField 와 동일 패턴 (외부 valueLabel). */
export default function ProductSelectField({ label = '제품 모델', ...rest }: Props) {
  return <EntitySelectField {...rest} label={label} config={productSelectConfig} />;
}
