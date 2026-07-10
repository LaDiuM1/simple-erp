import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import { useFileDownload } from '@/shared/api/fileDownload';
import type {
  DriveBrowseResponse,
  DriveFolderCreateRequest,
  DriveFolderRenameRequest,
} from '../types';

const driveApi = api.injectEndpoints({
  endpoints: (builder) => ({
    /** 현재 위치 탐색 — folderId 가 null 이면 루트. */
    browseDrive: builder.query<DriveBrowseResponse, number | null>({
      query: (folderId) => ({
        url: '/api/v1/drive',
        method: 'GET',
        params: folderId == null ? {} : { folderId },
      }),
      providesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
    createDriveFolder: builder.mutation<number, DriveFolderCreateRequest>({
      query: (body) => ({ url: '/api/v1/drive/folders', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
    renameDriveFolder: builder.mutation<void, { id: number; body: DriveFolderRenameRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/drive/folders/${id}`, method: 'PUT', data: body }),
      invalidatesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
    /** 빈 폴더만 삭제 가능 — 하위가 있으면 BE 가 409 (FOLDER_NOT_EMPTY). */
    deleteDriveFolder: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/drive/folders/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
    uploadDriveFile: builder.mutation<number, { folderId: number | null; form: FormData }>({
      query: ({ folderId, form }) => ({
        url: '/api/v1/drive/files',
        method: 'POST',
        data: form,
        params: folderId == null ? {} : { folderId },
      }),
      invalidatesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
    deleteDriveFile: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/drive/files/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Drive', id: 'BROWSE' }],
    }),
  }),
});

export const {
  useBrowseDriveQuery,
  useCreateDriveFolderMutation,
  useRenameDriveFolderMutation,
  useDeleteDriveFolderMutation,
  useUploadDriveFileMutation,
  useDeleteDriveFileMutation,
} = driveApi;

/** 드라이브 파일 다운로드 — binary 응답이라 RTK Query 대신 공용 fileDownload 훅으로 호출. */
export function useDriveFileDownload() {
  const download = useFileDownload();

  return useCallback(
    (fileId: number, fallbackName: string) =>
      download(`/api/v1/drive/files/${fileId}/download`, fallbackName),
    [download],
  );
}
