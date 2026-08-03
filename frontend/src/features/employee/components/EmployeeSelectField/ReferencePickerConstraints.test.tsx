import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from '@testing-library/react';
import EmployeeSelectField from './EmployeeSelectField';
import { useGetEmployeeReferencesQuery } from '@/features/employee/api/employeeApi';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField/CustomerSelectField';
import DepartmentSelectField from '@/features/department/components/DepartmentSelectField/DepartmentSelectField';
import EquipmentSelectField from '@/features/equipment/components/EquipmentSelectField/EquipmentSelectField';
import { useGetEquipmentReferencesQuery } from '@/features/equipment/api/equipmentApi';
import PositionSelectField from '@/features/position/components/PositionSelectField/PositionSelectField';
import ProductSelectField from '@/features/product/components/ProductSelectField/ProductSelectField';
import RoleSelectField from '@/features/role/components/RoleSelectField/RoleSelectField';
import SalesContactSelectField from '@/features/salesContact/components/SalesContactSelectField/SalesContactSelectField';

const entitySelect = vi.hoisted(() => ({
  props: null as null | {
    fixedQueryParams?: Record<string, unknown>;
    scopeKey?: string;
    config: {
      searchFilter: Array<{ key: string }>;
      column: Array<{
        key: string;
        mobilePrimary?: boolean;
        hideOnMobile?: boolean;
      }>;
      rowLabel: (row: unknown) => string;
      useSearchList: unknown;
    };
  },
}));

vi.mock('@/shared/ui/EntitySelectField', () => ({
  default: (props: typeof entitySelect.props) => {
    entitySelect.props = props;
    return null;
  },
}));

vi.mock('@/features/reference/api/referenceApi', () => ({
  useGetDepartmentsQuery: () => ({ data: [] }),
  useGetProductCategoriesQuery: () => ({ data: [] }),
  useGetPositionsQuery: () => ({ data: [] }),
  useGetRolesQuery: () => ({ data: [] }),
  useGetSuppliersQuery: () => ({ data: [] }),
}));

const commonProps = {
  value: '',
  valueLabel: '',
  onChange: vi.fn(),
};

describe('reference picker constraints', () => {
  beforeEach(() => {
    entitySelect.props = null;
  });

  it('신규 직원 선택은 ACTIVE 서버 조건을 강제하고 상태 필터를 숨긴다', () => {
    render(<EmployeeSelectField {...commonProps} />);

    expect(entitySelect.props?.config.useSearchList).toBe(useGetEmployeeReferencesQuery);
    expect(entitySelect.props?.fixedQueryParams).toEqual({ status: 'ACTIVE' });
    expect(entitySelect.props?.config.searchFilter.some((filter) => filter.key === 'status'))
      .toBe(false);
    expect(entitySelect.props?.config.searchFilter.map((filter) => filter.key)).toEqual([
      'nameKeyword',
      'departmentId',
      'positionId',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'departmentName',
      'positionName',
    ]);
  });

  it('과거 직원 선택도 상태 외 개인정보·인사 관리 필드를 노출하지 않는다', () => {
    render(<EmployeeSelectField {...commonProps} activeOnly={false} />);

    expect(entitySelect.props?.fixedQueryParams).toBeUndefined();
    expect(entitySelect.props?.config.searchFilter.map((filter) => filter.key)).toEqual([
      'nameKeyword',
      'departmentId',
      'positionId',
      'status',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'departmentName',
      'positionName',
      'status',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).not.toEqual(
      expect.arrayContaining(['loginId', 'birthDate', 'joinDate', 'roleName', 'email', 'phone']),
    );
  });

  it('AS 설비 선택은 고객사 조건을 강제한다', () => {
    render(<EquipmentSelectField {...commonProps} customerId="48" />);

    expect(entitySelect.props?.config.useSearchList).toBe(useGetEquipmentReferencesQuery);
    expect(entitySelect.props?.fixedQueryParams).toEqual({ customerId: 48 });
    expect(entitySelect.props?.scopeKey).toBe('customer:48');
    expect(entitySelect.props?.config.searchFilter.map((filter) => filter.key)).toEqual([
      'serialKeyword',
      'addressKeyword',
      'warranty',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'productModelName',
      'serialNo',
      'installAddress',
      'installedDate',
      'generalWarrantyEndDate',
    ]);
    expect(
      entitySelect.props?.config.column
        .filter((column) => !column.hideOnMobile)
        .map((column) => column.key),
    ).toContain('installAddress');
    expect(
      entitySelect.props?.config.rowLabel({
        productModelName: 'ALW-06-FLEX',
        serialNo: null,
        installAddress: '경기도 새온시 산업로 123',
      }),
    ).toBe('ALW-06-FLEX · 경기도 새온시 산업로 123');
  });

  it('영업 담당자 선택은 고객사 조건을 강제한다', () => {
    render(<SalesContactSelectField {...commonProps} customerId={48} />);

    expect(entitySelect.props?.fixedQueryParams).toEqual({ customerId: 48 });
    expect(entitySelect.props?.scopeKey).toBe('customer:48');
    expect(entitySelect.props?.config.searchFilter.map((filter) => filter.key)).toEqual([
      'nameKeyword',
      'emailKeyword',
      'phoneKeyword',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'currentPosition',
      'currentDepartment',
      'mobilePhone',
      'email',
    ]);
  });

  it('고객사가 고정되지 않은 영업 명부 선택만 현재 소속을 보여준다', () => {
    render(<SalesContactSelectField {...commonProps} />);

    expect(entitySelect.props?.fixedQueryParams).toBeUndefined();
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'currentCompanyName',
      'currentPosition',
      'currentDepartment',
      'mobilePhone',
      'email',
    ]);
  });

  it('고객사 picker는 실제 wrapper에서 식별 컬럼과 모바일 상태를 유지한다', () => {
    render(<CustomerSelectField {...commonProps} />);

    expect(entitySelect.props?.config.searchFilter.map((filter) => filter.key)).toEqual([
      'codeKeyword',
      'nameKeyword',
      'phoneKeyword',
      'type',
      'status',
    ]);
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'code',
      'representative',
      'phone',
      'type',
      'status',
    ]);
    expect(
      entitySelect.props?.config.column
        .filter((column) => !column.hideOnMobile)
        .map((column) => column.key),
    ).toContain('status');
    expect(entitySelect.props?.config.rowLabel({ name: '미르온정밀' })).toBe('미르온정밀');
  });

  it('부서·직책·권한 wrapper는 이름을 대표 정보로 둔 선택 컬럼을 전달한다', () => {
    const primaryKey = (columns: Array<{ key: string; mobilePrimary?: boolean }>) =>
      columns.find((column) => column.mobilePrimary)?.key ?? columns[0]?.key;

    render(<DepartmentSelectField value="" onChange={vi.fn()} />);
    expect(primaryKey(entitySelect.props?.config.column ?? [])).toBe('name');
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'code',
      'parentName',
    ]);
    expect(entitySelect.props?.config.rowLabel({ name: '기술지원팀' })).toBe('기술지원팀');

    render(<PositionSelectField value="" onChange={vi.fn()} />);
    expect(primaryKey(entitySelect.props?.config.column ?? [])).toBe('name');
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'code',
      'rankLevel',
    ]);

    render(<RoleSelectField value="" onChange={vi.fn()} />);
    expect(primaryKey(entitySelect.props?.config.column ?? [])).toBe('name');
    expect(entitySelect.props?.config.column.map((column) => column.key)).toEqual([
      'name',
      'code',
      'description',
    ]);
  });

  it('제품 picker는 실제 wrapper에서 모델을 대표 정보로 두고 모바일 사용 여부를 유지한다', () => {
    render(<ProductSelectField {...commonProps} />);

    const columns = entitySelect.props?.config.column ?? [];
    const primaryKey = columns.find((column) => column.mobilePrimary)?.key ?? columns[0]?.key;

    expect(primaryKey).toBe('modelName');
    expect(columns.map((column) => column.key)).toEqual([
      'modelName',
      'categoryName',
      'supplierName',
      'active',
    ]);
    expect(columns.filter((column) => !column.hideOnMobile).map((column) => column.key))
      .toContain('active');
    expect(entitySelect.props?.config.rowLabel({ modelName: 'ALW-06-FLEX' }))
      .toBe('ALW-06-FLEX');
    expect(entitySelect.props?.fixedQueryParams).toEqual({ active: 'true' });
  });
});
