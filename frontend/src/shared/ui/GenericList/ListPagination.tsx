import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import {
  PaginationBar,
  PaginationSpacer,
  StyledPagination,
  TotalCountText,
} from './ListPagination.styles';

interface Props {
  /** 0-base */
  page: number;
  totalPages: number;
  totalElements?: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
  /** 현재 페이지 결과가 준비되기 전에도 요청한 페이지 번호를 안정적으로 유지한다. */
  pending?: boolean;
}

/**
 * 목록 하단 페이지네이션 바. "총 N건" 카운트 + MUI Pagination.
 * 부모 surface 에 임베드되어 상단 구분선 + 위쪽 그림자로 시각 분리된다.
 */
export default function ListPagination({
  page,
  totalPages,
  totalElements,
  onPageChange,
  disabled = false,
  pending = false,
}: Props) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const pageCount = pending
    ? Math.max(totalPages, page + 1, 1)
    : Math.max(totalPages, 1);

  return (
    <PaginationBar>
      <TotalCountText>
        {totalElements != null ? `총 ${totalElements.toLocaleString()}건` : ''}
      </TotalCountText>
      <StyledPagination
        count={pageCount}
        page={page + 1}
        onChange={(_, next) => onPageChange(next - 1)}
        shape="rounded"
        size="small"
        siblingCount={isMobile ? 0 : 1}
        disabled={disabled || totalPages <= 1}
      />
      <PaginationSpacer />
    </PaginationBar>
  );
}
