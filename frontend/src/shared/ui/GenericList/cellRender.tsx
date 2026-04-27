import type { ReactNode } from 'react';
import Muted from '@/shared/ui/atoms/Muted';
import TruncatedCellContent from './TruncatedCellContent';
import type { ColumnConfig } from './types';

/**
 * 셀 원시 콘텐츠 — col.render 가 있으면 사용, 없으면 row[key] 노출 (null 은 Muted).
 * 모바일 카드 등 truncation 불필요한 곳에서 직접 사용.
 */
export function renderCellContent<TRow>(col: ColumnConfig<TRow>, row: TRow): ReactNode {
  if (col.render) return col.render(row);
  const value = (row as Record<string, unknown>)[col.key];
  if (value == null) return <Muted />;
  return value as ReactNode;
}

/**
 * 데스크탑 테이블 셀 콘텐츠 — `col.noTruncate` 가 아니면 ellipsis + hover Tooltip 처리.
 * Tooltip 텍스트는 row[key] 가 string/number 일 때만 추출 (복잡 render 의 경우 미노출).
 */
export function renderTruncatableCell<TRow>(col: ColumnConfig<TRow>, row: TRow): ReactNode {
  const content = renderCellContent(col, row);
  if (col.noTruncate) return content;
  return (
    <TruncatedCellContent tooltip={getTooltipText(col, row)} align={col.align ?? 'left'}>
      {content}
    </TruncatedCellContent>
  );
}

function getTooltipText<TRow>(col: ColumnConfig<TRow>, row: TRow): string | undefined {
  const value = (row as Record<string, unknown>)[col.key];
  if (typeof value === 'string') return value || undefined;
  if (typeof value === 'number') return String(value);
  return undefined;
}
