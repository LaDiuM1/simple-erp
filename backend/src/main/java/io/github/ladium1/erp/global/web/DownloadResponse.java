package io.github.ladium1.erp.global.web;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 바이너리 첨부 파일 다운로드 응답을 만드는 헬퍼.
 * 한글 파일명은 RFC 5987 방식으로 인코딩된다.
 */
public final class DownloadResponse {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private DownloadResponse() {
    }

    public static ResponseEntity<ByteArrayResource> attachment(byte[] bytes, String filename, MediaType mediaType) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    public static ResponseEntity<ByteArrayResource> xlsx(byte[] bytes, String filename) {
        return attachment(bytes, filename, XLSX);
    }
}
