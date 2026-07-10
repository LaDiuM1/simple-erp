package io.github.ladium1.erp.drive.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전사 공유 드라이브의 폴더.
 * <p>
 * parentId 로 트리를 구성한다 — null 이면 루트 직속. 파일 본체는 소유하지 않고
 * {@link DriveFile} 이 storage 파일 식별자를 참조한다.
 */
@Entity
@Getter
@Table(name = "drive_folders",
        indexes = @Index(name = "idx_drive_folders_parent_id", columnList = "parent_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveFolder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, comment = "폴더명")
    private String name;

    @Column(name = "parent_id", comment = "상위 폴더 식별자 — null 이면 루트 직속")
    private Long parentId;

    @Column(name = "created_by", nullable = false, comment = "생성한 직원 식별자")
    private Long createdBy;

    @Builder
    DriveFolder(String name, Long parentId, Long createdBy) {
        this.name = name;
        this.parentId = parentId;
        this.createdBy = createdBy;
    }

    public void rename(String name) {
        this.name = name;
    }
}
