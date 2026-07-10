import { styled } from '@mui/material/styles';

/** 이름 셀 공용 — table-layout: fixed 컬럼 폭 안에서 ellipsis 처리. */
export const FileNameText = styled('span')({
  display: 'block',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
});

/** 폴더 이름 — 아이콘 대신 텍스트 색으로 파일과 구분 (시각 차등은 텍스트 색만). */
export const FolderNameText = styled(FileNameText)(({ theme }) => ({
  color: theme.palette.primary.main,
  fontWeight: 600,
}));
