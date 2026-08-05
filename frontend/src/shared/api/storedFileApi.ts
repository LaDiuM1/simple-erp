import { api } from './baseApi';

/** BE StoredFileInfo (`POST /api/v1/files` 응답) */
export interface StoredFileInfo {
  id: number;
  originalName: string;
  contentType: string;
  size: number;
}

const storedFileApi = api.injectEndpoints({
  endpoints: (builder) => ({
    uploadStoredFile: builder.mutation<StoredFileInfo, File>({
      query: (file) => {
        const data = new FormData();
        data.append('file', file);
        return { url: '/api/v1/files', method: 'POST', data };
      },
    }),
  }),
});

export const { useUploadStoredFileMutation } = storedFileApi;
