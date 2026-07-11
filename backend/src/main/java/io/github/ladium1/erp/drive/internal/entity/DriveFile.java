package io.github.ladium1.erp.drive.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 드라이브에 올려진 파일의 배치 정보.
 * <p>
 * 본체 / 메타 (크기, contentType) 는 storage 모듈 소유 — 드라이브는 폴더 위치와 표시명만 소유한다.
 */
@Entity
@Getter
@Table(name = "drive_files",
        indexes = @Index(name = "idx_drive_files_folder_id", columnList = "folder_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 폴더 — null 이면 루트 직속.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private DriveFolder folder;

    @Column(name = "storage_file_id", nullable = false, comment = "storage 모듈 파일 식별자")
    private Long storageFileId;

    @Column(nullable = false, comment = "표시명 (업로드 시 원본 파일명)")
    private String name;

    @Column(name = "uploader_id", nullable = false, comment = "업로드한 직원 식별자")
    private Long uploaderId;

    @Builder
    DriveFile(DriveFolder folder, Long storageFileId, String name, Long uploaderId) {
        this.folder = folder;
        this.storageFileId = storageFileId;
        this.name = name;
        this.uploaderId = uploaderId;
    }
}
