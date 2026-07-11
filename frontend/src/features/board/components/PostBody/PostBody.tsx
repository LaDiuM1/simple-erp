import AttachFileRoundedIcon from '@mui/icons-material/AttachFileRounded';
import { useFileDownload } from '@/shared/api/fileDownload';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { PostDetail } from '@/features/board/types';
import {
  AttachmentLink,
  AttachmentList,
  BodyRoot,
  ContentText,
} from './PostBody.styles';

/**
 * 게시글 본문 + 첨부 목록 — 파일명 클릭 시 게시판 첨부 엔드포인트로 다운로드
 * (BE 가 게시글 소속 검증: GET /api/v1/boards/{postId}/attachments/{fileId}).
 */
export default function PostBody({ detail }: { detail: PostDetail }) {
  const download = useFileDownload();
  const snackbar = useSnackbar();

  const handleDownload = (fileId: number, name: string) => {
    download(`/api/v1/boards/${detail.id}/attachments/${fileId}`, name).catch(() => {
      snackbar.error('파일 다운로드에 실패했습니다.');
    });
  };

  return (
    <BodyRoot>
      <ContentText>{detail.content}</ContentText>
      {detail.attachments.length > 0 && (
        <AttachmentList>
          {detail.attachments.map((file) => (
            <AttachmentLink
              key={file.fileId}
              type="button"
              onClick={() => handleDownload(file.fileId, file.name)}
            >
              <AttachFileRoundedIcon sx={{ fontSize: '0.9375rem' }} />
              {file.name}
            </AttachmentLink>
          ))}
        </AttachmentList>
      )}
    </BodyRoot>
  );
}
