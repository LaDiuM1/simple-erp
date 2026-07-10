package io.github.ladium1.erp.drive.internal.dto;

/**
 * 파일 다운로드 결과 — 표시명은 DriveFile, contentType / 본체는 storage 에서 온다.
 */
public record DriveFileDownload(
        String name,
        String contentType,
        byte[] content
) {
}
