package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * 현재 canonical reset 시점 이후에 생긴 물리 파일의 계정/generation 누적량을 DB에서 계산한다.
 * singleton manifest row를 잠가 모든 upload transaction을 직렬화하므로 backend 재시작과 동시 요청으로
 * quota를 우회할 수 없다.
 */
@Component
public class DemoUploadQuotaGuard {

    private static final String LOCK_MANIFEST_SQL = """
            SELECT reset_at
            FROM demo_seed_manifest
            WHERE id = 1
            FOR UPDATE
            """;
    private static final String GENERATION_FILES_FOR_UPDATE_SQL = """
            SELECT uploader_id, size
            FROM stored_files
            WHERE created_at >= ?
            ORDER BY id
            FOR UPDATE
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DemoProperties properties;
    private final DiskSpaceProbe diskSpaceProbe;

    @Autowired
    public DemoUploadQuotaGuard(JdbcTemplate jdbcTemplate, DemoProperties properties) {
        this(jdbcTemplate, properties, DemoUploadQuotaGuard::inspectDiskSpace);
    }

    DemoUploadQuotaGuard(
            JdbcTemplate jdbcTemplate,
            DemoProperties properties,
            DiskSpaceProbe diskSpaceProbe
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.diskSpaceProbe = diskSpaceProbe;
    }

    public void assertUploadAllowed(Long uploaderId, long incomingBytes, Path storagePath) {
        if (!properties.isEnabled()) {
            return;
        }
        if (uploaderId == null || incomingBytes <= 0) {
            throw new BusinessException(DemoErrorCode.DEMO_UPLOAD_QUOTA_EXCEEDED);
        }

        assertDiskReserve(storagePath, incomingBytes);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }

        try {
            LocalDateTime resetAt = jdbcTemplate.queryForObject(
                    LOCK_MANIFEST_SQL,
                    LocalDateTime.class
            );
            if (resetAt == null) {
                throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
            }

            Usage accountUsage = new Usage(0, 0);
            Usage generationUsage = new Usage(0, 0);
            for (QuotaFile file : jdbcTemplate.query(
                    GENERATION_FILES_FOR_UPDATE_SQL,
                    (resultSet, rowNumber) -> new QuotaFile(
                            resultSet.getObject("uploader_id", Long.class),
                            resultSet.getLong("size")
                    ),
                    resetAt
            )) {
                if (file.size() < 0) {
                    throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
                }
                generationUsage = generationUsage.add(file.size());
                if (uploaderId.equals(file.uploaderId())) {
                    accountUsage = accountUsage.add(file.size());
                }
            }
            DemoProperties.Upload limits = properties.getUpload();
            if (exceeds(accountUsage, incomingBytes,
                    limits.getAccountQuotaBytes(), limits.getAccountQuotaFiles())
                    || exceeds(generationUsage, incomingBytes,
                    limits.getGenerationQuotaBytes(), limits.getGenerationQuotaFiles())) {
                throw new BusinessException(DemoErrorCode.DEMO_UPLOAD_QUOTA_EXCEEDED);
            }
        } catch (BusinessException guarded) {
            throw guarded;
        } catch (DataAccessException invalidQuotaState) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
    }

    public void assertDiskReserve(Path storagePath, long incomingBytes) {
        if (!properties.isEnabled()) {
            return;
        }
        if (storagePath == null || incomingBytes <= 0) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }

        try {
            DiskSpace space = diskSpaceProbe.inspect(storagePath);
            if (space.totalBytes() <= 0 || space.usableBytes() < 0) {
                throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
            }
            long ratioReserve = (long) Math.ceil(
                    space.totalBytes() * properties.getUpload().getMinFreeRatio()
            );
            long reserve = Math.max(properties.getUpload().getMinFreeBytes(), ratioReserve);
            if (incomingBytes > space.usableBytes()
                    || space.usableBytes() - incomingBytes < reserve) {
                throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
            }
        } catch (BusinessException guarded) {
            throw guarded;
        } catch (IOException | RuntimeException unavailable) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
    }

    private static boolean exceeds(Usage usage, long incomingBytes, long byteLimit, int fileLimit) {
        return usage.fileCount() >= fileLimit
                || usage.totalBytes() > byteLimit
                || incomingBytes > byteLimit - usage.totalBytes();
    }

    private static DiskSpace inspectDiskSpace(Path storagePath) throws IOException {
        Path existing = storagePath.toAbsolutePath().normalize();
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing == null) {
            throw new IOException("storage path에 존재하는 상위 경로가 없습니다.");
        }
        FileStore fileStore = Files.getFileStore(existing);
        return new DiskSpace(fileStore.getTotalSpace(), fileStore.getUsableSpace());
    }

    @FunctionalInterface
    interface DiskSpaceProbe {
        DiskSpace inspect(Path path) throws IOException;
    }

    record DiskSpace(long totalBytes, long usableBytes) {
    }

    private record QuotaFile(Long uploaderId, long size) {
    }

    private record Usage(long fileCount, long totalBytes) {
        Usage add(long bytes) {
            try {
                return new Usage(Math.addExact(fileCount, 1), Math.addExact(totalBytes, bytes));
            } catch (ArithmeticException overflow) {
                throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
            }
        }
    }
}
