package io.github.ladium1.erp.global.storage;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 저장된 파일의 메타 정보 — 모듈 외부 노출용.
 */
@Builder
public record StoredFileInfo(
        Long id,
        String originalName,
        String contentType,
        long size,
        Long uploaderId,
        LocalDateTime createdAt
) {
}
