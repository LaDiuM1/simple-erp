export interface ApiResponse<T> {
  status: number;
  message: string;
  code?: string | null;
  data: T;
}

export interface ApiError {
  status: number;
  message: string;
  code?: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface PageRequest {
  page: number;
  size: number;
  sort?: string;
}
