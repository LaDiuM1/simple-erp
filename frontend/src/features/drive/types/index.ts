/**
 * 백엔드 io.github.ladium1.erp.drive.internal.dto 의 DTO 미러.
 * BE 변경 시 함께 갱신.
 */

export interface DriveBreadcrumbItem {
  id: number;
  name: string;
}

export interface DriveFolderItem {
  id: number;
  name: string;
  createdAt: string;
}

export interface DriveFileItem {
  id: number;
  name: string;
  size: number;
  uploaderId: number;
  uploaderName: string | null;
  createdAt: string;
}

export interface DriveBrowseResponse {
  /** 루트 → 현재 폴더 순서 (자기 자신 포함). 루트 탐색이면 빈 배열. */
  breadcrumb: DriveBreadcrumbItem[];
  folders: DriveFolderItem[];
  files: DriveFileItem[];
}

export interface DriveFolderCreateRequest {
  name: string;
  /** 상위 폴더 식별자 — null 이면 루트에 생성. */
  parentId: number | null;
}

export interface DriveFolderRenameRequest {
  name: string;
}
