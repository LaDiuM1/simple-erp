package io.github.ladium1.erp.global.storage.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 저장 파일 메타 — 본체는 {base-path}/{yyyy}/{MM}/{storedName} 디스크에 보관.
 */
@Entity
@Table(name = "stored_files")
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

    @Builder
    private StoredFile(String originalName, String storedName, String contentType, long size, Long uploaderId) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.contentType = contentType;
        this.size = size;
        this.uploaderId = uploaderId;
    }
}
