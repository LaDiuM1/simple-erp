import { useState } from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import {
  BodyCell,
  BodyRow,
  EmptyStateContainer,
  EmptyStateText,
  HeaderCell,
  StyledTableContainer,
} from '@/shared/ui/GenericList';
import {
  MobileCard,
  MobileCardList,
  MobileLabelCell,
  MobileValueCell,
  TabBar,
  TabBarLeft,
  TabBarRight,
  TabButton,
  TabContent,
  TabCount,
  TabTableScrollArea,
  TabbedRoot,
} from './GenericTabbedTable.styles';
import type { AnyTabbedTab, TabbedTableColumn } from './types';

interface Props {
  tabs: AnyTabbedTab[];
  /** 초기 활성 탭 — 미설정 시 첫 번째 탭. */
  defaultTabKey?: string;
}

/**
 * 매트릭스(테이블) 톤의 탭 컨테이너. 탭마다 column config + rows 가 다르며 각 탭은 독립 표.
 * 데스크탑: 표 (HeaderCell / BodyCell / BodyRow) — 시각 톤이 GenericList 와 동일.
 * 모바일 (md 미만): 행 1개 → 카드 1개. 카드 내부는 [label][value] 매트릭스 (GenericDetailModal 와 동일 톤).
 * 모든 탭은 빈 상태에서도 노출 — 페이지 구조가 안정적.
 * 데이터 로딩 / 에러 분기는 outer 컴포넌트에서 처리 (CLAUDE.md hook 정책) — 탭은 항상 보장된 rows 를 받는다.
 */
export default function GenericTabbedTable({ tabs, defaultTabKey }: Props) {
  const [activeKey, setActiveKey] = useState(defaultTabKey ?? tabs[0]?.key);
  const active = tabs.find((t) => t.key === activeKey) ?? tabs[0];
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  if (!active) return null;

  return (
    <TabbedRoot>
      <TabBar>
        <TabBarLeft>
          {tabs.map((t) => (
            <TabButton
              key={t.key}
              type="button"
              isActive={t.key === active.key}
              onClick={() => setActiveKey(t.key)}
            >
              {t.label}
              {t.count !== undefined && t.count !== null && t.count !== '' && (
                <TabCount>{t.count}</TabCount>
              )}
            </TabButton>
          ))}
        </TabBarLeft>
        {active.rightSlot && <TabBarRight>{active.rightSlot}</TabBarRight>}
      </TabBar>
      <TabContent>
        {active.rows.length === 0 ? (
          <EmptyStateContainer>
            <EmptyStateText>
              {active.emptyMessage ?? '표시할 데이터가 없습니다.'}
            </EmptyStateText>
          </EmptyStateContainer>
        ) : isMobile ? (
          <MobileBody tab={active} />
        ) : (
          <TabTableScrollArea>
            <StyledTableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    {active.columns.map((c) => (
                      <HeaderCell
                        key={c.key}
                        align={c.align}
                        sx={c.width !== undefined ? { width: c.width } : undefined}
                      >
                        {c.header}
                      </HeaderCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {active.rows.map((row, idx) => {
                    const key = active.rowKey ? active.rowKey(row) : idx;
                    const sx = active.rowSx?.(row);
                    const onClick = active.onRowClick;
                    return (
                      <BodyRow
                        key={key}
                        clickable={!!onClick}
                        onClick={onClick ? () => onClick(row) : undefined}
                        sx={sx}
                      >
                        {active.columns.map((c) => (
                          <BodyCell key={c.key} align={c.align}>
                            {c.render(row)}
                          </BodyCell>
                        ))}
                      </BodyRow>
                    );
                  })}
                </TableBody>
              </Table>
            </StyledTableContainer>
          </TabTableScrollArea>
        )}
      </TabContent>
    </TabbedRoot>
  );
}

function MobileBody({ tab }: { tab: AnyTabbedTab }) {
  const onClick = tab.onRowClick;
  return (
    <MobileCardList>
      {tab.rows.map((row, idx) => {
        const key = tab.rowKey ? tab.rowKey(row) : idx;
        const sx = tab.rowSx?.(row);
        return (
          <MobileCard
            key={key}
            clickable={!!onClick}
            onClick={onClick ? () => onClick(row) : undefined}
            sx={sx}
          >
            {tab.columns.map((c: TabbedTableColumn<unknown>) => (
              <MobileFieldRow key={c.key} column={c} row={row} />
            ))}
          </MobileCard>
        );
      })}
    </MobileCardList>
  );
}

function MobileFieldRow({
  column,
  row,
}: {
  column: TabbedTableColumn<unknown>;
  row: unknown;
}) {
  return (
    <>
      <MobileLabelCell>{column.header || ' '}</MobileLabelCell>
      <MobileValueCell>{column.render(row)}</MobileValueCell>
    </>
  );
}
