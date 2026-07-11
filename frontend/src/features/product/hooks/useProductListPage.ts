import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteProductMutation,
  useDeleteProductsMutation,
  useGetProductsSummaryQuery,
} from '@/features/product/api/productApi';
import {
  type ProductListFilters,
  type ProductSummary,
} from '@/features/product/types';

/**
 * 제품 모델 목록 page hook — api + headerActions 묶음. 페이지는 destructure 후 GenericList 에 prop 전달.
 */
export function useProductListPage(): {
  api: ListApiConfig<ProductSummary, ProductListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<ProductSummary, ProductListFilters> = {
    menuCode: MENU_CODE.PRODUCTS,
    useList: useGetProductsSummaryQuery,
    useDelete: useDeleteProductMutation,
    useBulkDelete: useDeleteProductsMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.PRODUCTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.PRODUCTS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '제품 모델 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.PRODUCTS]}/new`),
      menuCode: MENU_CODE.PRODUCTS,
    },
  ];

  return { api, headerActions };
}
