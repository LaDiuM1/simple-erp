package io.github.ladium1.erp.salescontact.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
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

/**
 * 컨택 경로 마스터 — 영업 명부의 출처 분류. salescontact 모듈의 sub-master 로,
 * 영업 명부 목록 페이지의 보조 모달로만 관리된다 (별도 메뉴 없음).
 * <p>
 * EXHIBITION: 전시회 / REFERRAL: 소개 / WEB: 인터넷·홈페이지 / OTHER: 기타.
 */
@Entity
@Getter
@Table(name = "acquisition_sources",
        indexes = {
                @Index(name = "idx_acquisition_sources_type", columnList = "type"),
                @Index(name = "uk_acquisition_sources_name", columnList = "name", unique = true)
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcquisitionSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100, comment = "컨택 경로 이름 — 전시회 명 / 소개자 명함명 등")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, comment = "분류")
    private AcquisitionSourceType type;

    @Column(length = 500, comment = "설명 / 비고 — 개최년도 / 장소 / 자유 메모")
    private String description;

    @Builder
    AcquisitionSource(String name, AcquisitionSourceType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }
}
