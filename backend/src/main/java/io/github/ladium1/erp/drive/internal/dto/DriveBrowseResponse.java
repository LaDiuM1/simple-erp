package io.github.ladium1.erp.drive.internal.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 드라이브 탐색 응답 — 현재 위치의 breadcrumb + 하위 폴더 / 파일 목록.
 */
public record DriveBrowseResponse(
        /**
         * 루트 → 현재 폴더 순서 (자기 자신 포함). 루트 탐색이면 빈 리스트.
         */
        List<BreadcrumbItem> breadcrumb,
        List<FolderItem> folders,
        List<FileItem> files
) {

    public record BreadcrumbItem(Long id, String name) {
    }

    public record FolderItem(Long id, String name, LocalDateTime createdAt) {
    }

    public record FileItem(
            Long id,
            String name,
            long size,
            Long uploaderId,
            String uploaderName,
            LocalDateTime createdAt
    ) {
    }
}
