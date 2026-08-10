package io.github.ladium1.erp.global.storage.internal.service;

import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileOwnerType;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFileStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 소유권 컬럼 도입 전에 저장된 파일을 기존 업무 참조로 복원한다.
 * 하나의 파일이 서로 다른 업무 레코드에서 발견되면 임의로 선택하지 않고 부팅을 중단한다.
 */
@Slf4j
@Component
@Order(10)
@ConditionalOnProperty(name = "app.schema-maintenance.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class FileOwnershipBackfill implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        List<StoredRow> storedRows = loadStoredRows();
        List<FileReference> references = loadReferences();
        List<BackfillUpdate> updates = plan(storedRows, references);

        updates.forEach(this::apply);
        ensureStatusConstraint();

        if (!updates.isEmpty()) {
            log.info("기존 저장 파일 소유권 복원 완료: {}건", updates.size());
        }
    }

    @SuppressWarnings("unchecked")
    private List<StoredRow> loadStoredRows() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, status, owner_type, owner_id, uploader_id FROM stored_files ORDER BY id"
        ).getResultList();
        return rows.stream()
                .map(row -> new StoredRow(
                        ((Number) row[0]).longValue(),
                        stringValue(row[1]),
                        stringValue(row[2]),
                        row[3] == null ? null : ((Number) row[3]).longValue(),
                        row[4] == null ? null : ((Number) row[4]).longValue()
                ))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<FileReference> loadReferences() {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT file_id, owner_type, owner_id, uploader_id
                FROM (
                    SELECT a.file_id, 'BOARD_POST' AS owner_type, a.post_id AS owner_id,
                           p.author_id AS uploader_id
                    FROM post_attachment_files a
                    JOIN posts p ON p.id = a.post_id
                    UNION ALL
                    SELECT a.file_id,
                           CASE WHEN d.doc_type = 'EXPENSE' THEN 'EXPENSE_CLAIM' ELSE 'APPROVAL_DOCUMENT' END,
                           CASE WHEN d.doc_type = 'EXPENSE' THEN d.ref_id ELSE d.id END,
                           d.drafter_id
                    FROM approval_document_attachments a
                    JOIN approval_documents d ON d.id = a.document_id
                    UNION ALL
                    SELECT i.receipt_file_id, 'EXPENSE_CLAIM', i.claim_id, c.claimant_id
                    FROM expense_items i
                    JOIN expense_claims c ON c.id = i.claim_id
                    WHERE i.receipt_file_id IS NOT NULL
                    UNION ALL
                    SELECT storage_file_id, 'DRIVE_FILE', id, uploader_id
                    FROM drive_files
                ) file_references
                ORDER BY file_id, owner_type, owner_id
                """).getResultList();
        return rows.stream().map(FileOwnershipBackfill::toReference).toList();
    }

    static List<BackfillUpdate> plan(List<StoredRow> storedRows, List<FileReference> references) {
        Map<Long, StoredRow> storedById = new LinkedHashMap<>();
        storedRows.forEach(row -> storedById.put(row.id(), row));

        Map<Long, Set<ExpectedClaim>> claimsByFileId = new LinkedHashMap<>();
        for (FileReference reference : references) {
            if (!storedById.containsKey(reference.fileId())) {
                throw conflict("업무 레코드가 존재하지 않는 저장 파일을 참조합니다", reference.fileId());
            }
            claimsByFileId.computeIfAbsent(reference.fileId(), ignored -> new LinkedHashSet<>())
                    .add(new ExpectedClaim(reference.owner(), reference.uploaderId()));
        }

        List<BackfillUpdate> updates = new ArrayList<>();
        for (StoredRow row : storedRows) {
            Set<ExpectedClaim> expectedClaims = claimsByFileId.getOrDefault(row.id(), Set.of());
            if (expectedClaims.size() > 1) {
                throw conflict("하나의 저장 파일에 서로 다른 소유권 정보가 연결되어 있습니다", row.id());
            }
            ExpectedClaim expectedClaim = expectedClaims.stream().findFirst().orElse(null);
            FileOwner referencedOwner = expectedClaim == null ? null : expectedClaim.owner();
            FileOwner recordedOwner = recordedOwner(row);

            if (row.status() == null) {
                if (recordedOwner != null && !recordedOwner.equals(referencedOwner)) {
                    throw conflict("저장된 소유권과 업무 참조가 일치하지 않습니다", row.id());
                }
                updates.add(referencedOwner == null
                        ? BackfillUpdate.pending(row.id(), row.uploaderId())
                        : BackfillUpdate.claimed(row.id(), referencedOwner,
                                verifiedUploader(row, expectedClaim)));
                continue;
            }

            StoredFileStatus status = parseStatus(row);
            if (status == StoredFileStatus.PENDING) {
                if (recordedOwner != null) {
                    throw conflict("미연결 파일에 업무 소유권이 남아 있습니다", row.id());
                }
                if (referencedOwner != null) {
                    updates.add(BackfillUpdate.claimed(
                            row.id(),
                            referencedOwner,
                            verifiedRollbackUploader(row, expectedClaim)
                    ));
                }
                continue;
            }
            if (status == StoredFileStatus.CLAIMED) {
                if (referencedOwner == null || (recordedOwner != null && !recordedOwner.equals(referencedOwner))) {
                    throw conflict("연결 파일의 소유권을 복원할 수 없습니다", row.id());
                }
                Long uploaderId = verifiedUploader(row, expectedClaim);
                if (recordedOwner == null || row.uploaderId() == null) {
                    updates.add(BackfillUpdate.claimed(row.id(), referencedOwner, uploaderId));
                }
                continue;
            }
            if (recordedOwner == null || referencedOwner != null) {
                throw conflict("삭제 대기 파일의 소유권 상태가 일관되지 않습니다", row.id());
            }
        }
        return List.copyOf(updates);
    }

    private void apply(BackfillUpdate update) {
        em.createNativeQuery("""
                UPDATE stored_files
                SET status = :status, owner_type = :ownerType, owner_id = :ownerId,
                    uploader_id = :uploaderId
                WHERE id = :id
                """)
                .setParameter("status", update.status().name())
                .setParameter("ownerType", update.owner() == null ? null : update.owner().type().name())
                .setParameter("ownerId", update.owner() == null ? null : update.owner().id())
                .setParameter("uploaderId", update.uploaderId())
                .setParameter("id", update.fileId())
                .executeUpdate();
    }

    private void ensureStatusConstraint() {
        Object[] constraint = (Object[]) em.createNativeQuery("""
                SELECT IS_NULLABLE, COLUMN_DEFAULT
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'stored_files'
                  AND COLUMN_NAME = 'status'
                """).getSingleResult();
        if (requiresStatusConstraint(stringValue(constraint[0]), stringValue(constraint[1]))) {
            em.createNativeQuery("""
                    ALTER TABLE stored_files
                    MODIFY status VARCHAR(255) NOT NULL DEFAULT 'PENDING' COMMENT '파일 생명주기 상태'
                    """).executeUpdate();
        }
    }

    static boolean requiresStatusConstraint(String nullable, String columnDefault) {
        String normalizedDefault = columnDefault == null ? null : columnDefault.replace("'", "");
        return !"NO".equals(nullable) || !StoredFileStatus.PENDING.name().equals(normalizedDefault);
    }

    private static FileReference toReference(Object[] row) {
        Long fileId = ((Number) row[0]).longValue();
        if (row[1] == null || row[2] == null || row[3] == null) {
            throw conflict("업무 참조에서 파일 소유자를 결정할 수 없습니다", fileId);
        }
        try {
            return new FileReference(
                    fileId,
                    new FileOwner(
                            FileOwnerType.valueOf(row[1].toString()),
                            ((Number) row[2]).longValue()
                    ),
                    ((Number) row[3]).longValue()
            );
        } catch (IllegalArgumentException exception) {
            throw conflict("업무 참조의 파일 소유자가 올바르지 않습니다", fileId);
        }
    }

    private static FileOwner recordedOwner(StoredRow row) {
        if (row.ownerType() == null && row.ownerId() == null) {
            return null;
        }
        if (row.ownerType() == null || row.ownerId() == null) {
            throw conflict("파일 소유권 컬럼이 일부만 기록되어 있습니다", row.id());
        }
        try {
            return new FileOwner(FileOwnerType.valueOf(row.ownerType()), row.ownerId());
        } catch (IllegalArgumentException exception) {
            throw conflict("알 수 없는 파일 소유자 유형입니다", row.id());
        }
    }

    private static StoredFileStatus parseStatus(StoredRow row) {
        try {
            return StoredFileStatus.valueOf(row.status());
        } catch (IllegalArgumentException exception) {
            throw conflict("알 수 없는 파일 상태입니다", row.id());
        }
    }

    private static Long verifiedUploader(StoredRow row, ExpectedClaim expectedClaim) {
        if (expectedClaim == null || expectedClaim.uploaderId() == null || expectedClaim.uploaderId() <= 0) {
            throw conflict("업무 참조에서 업로더를 결정할 수 없습니다", row.id());
        }
        if (row.uploaderId() != null && !row.uploaderId().equals(expectedClaim.uploaderId())) {
            throw conflict("저장된 업로더와 업무 소유자가 일치하지 않습니다", row.id());
        }
        return expectedClaim.uploaderId();
    }

    private static Long verifiedRollbackUploader(StoredRow row, ExpectedClaim expectedClaim) {
        if (expectedClaim == null || expectedClaim.uploaderId() == null || row.uploaderId() == null
                || !row.uploaderId().equals(expectedClaim.uploaderId())) {
            throw conflict("롤백 생성 파일의 업로더와 업무 소유자가 일치하지 않습니다", row.id());
        }
        return row.uploaderId();
    }

    private static IllegalStateException conflict(String message, Long fileId) {
        return new IllegalStateException(message + ": fileId=" + fileId);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    record StoredRow(Long id, String status, String ownerType, Long ownerId, Long uploaderId) {
    }

    record FileReference(Long fileId, FileOwner owner, Long uploaderId) {
    }

    record ExpectedClaim(FileOwner owner, Long uploaderId) {
    }

    record BackfillUpdate(Long fileId, StoredFileStatus status, FileOwner owner, Long uploaderId) {

        static BackfillUpdate pending(Long fileId, Long uploaderId) {
            return new BackfillUpdate(fileId, StoredFileStatus.PENDING, null, uploaderId);
        }

        static BackfillUpdate claimed(Long fileId, FileOwner owner, Long uploaderId) {
            return new BackfillUpdate(fileId, StoredFileStatus.CLAIMED, owner, uploaderId);
        }
    }
}
