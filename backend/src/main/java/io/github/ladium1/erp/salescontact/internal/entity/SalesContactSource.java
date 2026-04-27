package io.github.ladium1.erp.salescontact.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 영업 명부 ↔ 컨택 경로 N:N 정션. 메타데이터 없는 단순 링크.
 */
@Entity
@Getter
@Table(name = "sales_contact_sources",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sales_contact_sources_contact_source",
                columnNames = {"contact_id", "source_id"}
        ),
        indexes = {
                @Index(name = "idx_sales_contact_sources_contact_id", columnList = "contact_id"),
                @Index(name = "idx_sales_contact_sources_source_id", columnList = "source_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesContactSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_id", nullable = false, comment = "영업 명부 식별자")
    private Long contactId;

    @Column(name = "source_id", nullable = false, comment = "컨택 경로 식별자")
    private Long sourceId;

    @Builder
    SalesContactSource(Long contactId, Long sourceId) {
        this.contactId = contactId;
        this.sourceId = sourceId;
    }
}
