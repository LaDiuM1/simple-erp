import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';

/**
 * 모달 본문 — 필터 / 테이블 / 페이지네이션을 block flow 로 세로 stack.
 * 좁은 뷰포트에서 컨텐츠가 paper 보다 커지면 본문 자체가 스크롤. filter / pagination 은
 * `position: sticky` 로 양 끝에 고정되어 스크롤 중에도 항상 노출 (요구사항).
 * `min-height: 0` 은 paper (flex column) 안에서 ModalContent 가 컨텐츠 미만으로 줄어들 수 있게 함.
 */
export const ModalContent = styled(DialogContent)(({ theme }) => ({
  padding: 0,
  minHeight: 0,
  overflow: 'auto',
  borderTop: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

/** 타이틀 + 우측 헤더 액션 (예: 추가 버튼) 을 한 줄에 배치하는 DialogTitle. */
export const ModalTitleRow = styled(DialogTitle)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '1rem',
});

/** 필터바 영역 — 모달 본문 스크롤 시 상단에 고정. */
export const ModalFilterArea = styled(Box)(({ theme }) => ({
  position: 'sticky',
  top: 0,
  zIndex: 1,
  padding: '0.875rem 1rem',
  backgroundColor: theme.palette.background.paper,
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

/** 본문 외 고정 영역 (페이지네이션 / 선택 트레이) — 모달 본문 스크롤 시 하단에 고정. */
export const ModalFixedRow = styled(Box)(({ theme }) => ({
  position: 'sticky',
  bottom: 0,
  zIndex: 1,
  backgroundColor: theme.palette.background.paper,
}));

/**
 * 본문 (테이블 / 카드) 영역.
 *
 *  - 데스크탑: SearchTable 이 `ROW_HEIGHT × pageSize + HEADER_HEIGHT` 를 최소 높이로 유지해
 *    데이터 양 / 페이지 이동에 따른 흔들림을 막고, 가로 스크롤바가 필요하면 그만큼 확장한다.
 *  - 모바일: 자연 stack (카드 합).
 *
 * 고정 폭 열을 축소해야 하는 중간 폭에서는 가로 스크롤만 이 영역이 담당한다. 세로 스크롤은
 * ModalContent 가 단독으로 흡수해 filter / pagination 의 sticky 동작을 유지한다.
 */
export const SearchTableArea = styled(Box)({
  position: 'relative',
  overflowX: 'auto',
  overflowY: 'hidden',
});
