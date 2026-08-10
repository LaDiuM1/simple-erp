package io.github.ladium1.erp.global.storage.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** 미연결 업로드와 삭제 예약 파일을 작은 묶음으로 정리한다. */
@Slf4j
@Component
@ConditionalOnProperty(name = "erp.storage.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class FileStorageCleanupJob {

    private final FileStorageService fileStorageService;
    private final Clock businessClock;

    private final Duration pendingTtl;

    public FileStorageCleanupJob(
            FileStorageService fileStorageService,
            Clock businessClock,
            @Value("${erp.storage.pending-ttl:PT24H}") Duration pendingTtl
    ) {
        this.fileStorageService = fileStorageService;
        this.businessClock = businessClock;
        this.pendingTtl = Objects.requireNonNull(pendingTtl, "미연결 파일 보존 기간은 필수입니다.");
        if (pendingTtl.isZero() || pendingTtl.isNegative()) {
            throw new IllegalArgumentException("미연결 파일 보존 기간은 0보다 커야 합니다.");
        }
    }

    @Scheduled(
            initialDelayString = "${erp.storage.cleanup-initial-delay:PT10M}",
            fixedDelayString = "${erp.storage.cleanup-interval:PT10M}"
    )
    public void cleanup() {
        LocalDateTime expiration = LocalDateTime.ofInstant(
                businessClock.instant().minus(pendingTtl),
                businessClock.getZone()
        );
        int expired = fileStorageService.markExpiredPendingForDeletion(expiration);
        int deleted = fileStorageService.deletePendingFiles();
        if (expired > 0 || deleted > 0) {
            log.info("파일 저장소 정리 완료: expired={}, deleted={}", expired, deleted);
        }
    }
}
