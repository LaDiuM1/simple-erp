package io.github.ladium1.erp.global.storage.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import io.github.ladium1.erp.global.storage.FileOwner;
import io.github.ladium1.erp.global.storage.FileOwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 저장 파일 메타 — 본체는 {base-path}/{yyyy}/{MM}/{storedName} 디스크에 보관.
 */
@Entity
@Table(name = "stored_files", indexes =
        @Index(name = "idx_stored_files_status_created_at", columnList = "status, created_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true, length = 36)
    private String storedName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(name = "uploader_id")
    private Long uploaderId;

    // 기존 테이블은 시작 시 소유권 복원 뒤 NOT NULL 로 강화한다.
    @Enumerated(EnumType.STRING)
    @Column(comment = "파일 생명주기 상태")
    private StoredFileStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", comment = "파일을 독점 소유하는 업무 레코드 유형")
    private FileOwnerType ownerType;

    @Column(name = "owner_id", comment = "파일을 독점 소유하는 업무 레코드 식별자")
    private Long ownerId;

    @Builder
    private StoredFile(String originalName, String storedName, String contentType, long size, Long uploaderId) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.size = size;
        this.uploaderId = uploaderId;
        this.status = StoredFileStatus.PENDING;
    }

    public boolean canBeClaimedBy(FileOwner owner, Long expectedUploaderId) {
        if (owner == null || !Objects.equals(uploaderId, expectedUploaderId)) {
            return false;
        }
        return status == StoredFileStatus.PENDING
                || (status == StoredFileStatus.CLAIMED && isOwnedBy(owner));
    }

    public void claim(FileOwner owner) {
        this.status = StoredFileStatus.CLAIMED;
        this.ownerType = owner.type();
        this.ownerId = owner.id();
    }

    public boolean isClaimedBy(FileOwner owner) {
        return owner != null && status == StoredFileStatus.CLAIMED && isOwnedBy(owner);
    }

    public boolean canRequestDeletionBy(FileOwner owner) {
        return owner != null
                && (status == StoredFileStatus.CLAIMED || status == StoredFileStatus.DELETE_PENDING)
                && isOwnedBy(owner);
    }

    public void requestDeletion() {
        this.status = StoredFileStatus.DELETE_PENDING;
    }

    public boolean isPending() {
        return status == StoredFileStatus.PENDING;
    }

    private boolean isOwnedBy(FileOwner owner) {
        return owner.type() == ownerType && owner.id().equals(ownerId);
    }
}
