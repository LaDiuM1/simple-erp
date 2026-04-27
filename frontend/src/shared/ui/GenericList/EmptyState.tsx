import InboxIcon from '@mui/icons-material/InboxOutlined';
import { EmptyStateContainer, EmptyStateText } from './ListTable.styles';

interface Props {
  message: string;
  /** 아이콘 크기 (px). 기본 44 — 검색 모달처럼 좁은 영역에서 40 등으로 축소 가능. */
  iconSize?: number;
}

/** 데이터 없음 안내 — 데스크탑 테이블 / 모바일 카드 / 검색 모달 공용. */
export default function EmptyState({ message, iconSize = 44 }: Props) {
  return (
    <EmptyStateContainer>
      <InboxIcon sx={{ fontSize: iconSize, color: 'text.disabled' }} />
      <EmptyStateText>{message}</EmptyStateText>
    </EmptyStateContainer>
  );
}
