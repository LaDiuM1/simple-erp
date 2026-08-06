import { styled } from '@mui/material/styles';

/** 이름 셀 공용 — table-layout: fixed 컬럼 폭 안에서 ellipsis 처리. */
export const FileNameButton = styled('button')(({ theme }) => ({
  appearance: 'none',
  display: 'block',
  width: '100%',
  minWidth: 0,
  padding: 0,
  border: 0,
  background: 'none',
  color: 'inherit',
  font: 'inherit',
  textAlign: 'left',
  cursor: 'pointer',
  borderRadius: 3,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.main}`,
    outlineOffset: 2,
  },
}));

/** 폴더 이름 — 아이콘 대신 텍스트 색으로 파일과 구분 (시각 차등은 텍스트 색만). */
export const FolderNameButton = styled(FileNameButton)(({ theme }) => ({
  color: theme.palette.primary.main,
  fontWeight: 600,
}));
