import type { AttachedFile } from '@/shared/ui/FileAttachField';

export type BoardCategory = 'MEETING' | 'NOTICE' | 'FREE';

export const BOARD_CATEGORY_LABELS: Record<BoardCategory, string> = {
  MEETING: '회의록',
  NOTICE: '공지사항',
  FREE: '자유',
};

export const BOARD_CATEGORY_OPTIONS: { value: BoardCategory; label: string }[] = [
  { value: 'MEETING', label: '회의록' },
  { value: 'NOTICE', label: '공지사항' },
  { value: 'FREE', label: '자유' },
];

export interface PostSummary {
  id: number;
  category: BoardCategory;
  title: string;
  authorName: string | null;
  commentCount: number;
  createdAt: string;
}

export interface PostAttachment {
  fileId: number;
  name: string;
  size: number;
}

export interface PostComment {
  id: number;
  authorId: number;
  authorName: string | null;
  content: string;
  createdAt: string;
}

export interface PostDetail {
  id: number;
  category: BoardCategory;
  title: string;
  content: string;
  authorId: number;
  authorName: string | null;
  createdAt: string;
  attachments: PostAttachment[];
  comments: PostComment[];
}

export interface PostCreateRequest {
  category: BoardCategory;
  title: string;
  content: string;
  attachmentFileIds: number[];
}

export type PostUpdateRequest = PostCreateRequest;

export interface CommentCreateRequest {
  content: string;
}

export interface PostSearchParams {
  category?: BoardCategory | null;
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type PostListFilters = Omit<PostSearchParams, 'page' | 'size' | 'sort'>;

export interface BoardFormValues {
  category: BoardCategory;
  title: string;
  content: string;
  attachments: AttachedFile[];
}

export const EMPTY_BOARD_FORM: BoardFormValues = {
  category: 'FREE',
  title: '',
  content: '',
  attachments: [],
};

export function postDetailToFormValues(d: PostDetail): BoardFormValues {
  return {
    category: d.category,
    title: d.title,
    content: d.content,
    attachments: d.attachments.map((a) => ({ fileId: a.fileId, name: a.name, size: a.size })),
  };
}

export function boardFormToCreateRequest(v: BoardFormValues): PostCreateRequest {
  return {
    category: v.category,
    title: v.title.trim(),
    content: v.content,
    attachmentFileIds: v.attachments.map((f) => f.fileId),
  };
}

export function boardFormToUpdateRequest(v: BoardFormValues): PostUpdateRequest {
  return boardFormToCreateRequest(v);
}
