package io.github.ladium1.erp.global.storage.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FileStorageCleanupJobTest {

    @Mock
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("24시간 지난 미연결 파일을 삭제 대기로 바꾼 뒤 물리 정리")
    void cleanup_uses_pending_ttl_before_deleting() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        Clock clock = Clock.fixed(Instant.parse("2026-08-13T03:00:00Z"), zone);
        FileStorageCleanupJob job = new FileStorageCleanupJob(
                fileStorageService, clock, Duration.ofHours(24));

        job.cleanup();

        LocalDateTime expiration = LocalDateTime.of(2026, 8, 12, 12, 0);
        var order = inOrder(fileStorageService);
        order.verify(fileStorageService).markExpiredPendingForDeletion(expiration);
        order.verify(fileStorageService).deletePendingFiles();
    }
}
