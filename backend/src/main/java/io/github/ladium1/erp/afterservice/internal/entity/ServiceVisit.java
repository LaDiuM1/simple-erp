package io.github.ladium1.erp.afterservice.internal.entity;

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

import java.time.LocalDate;

/**
 * AS 방문 일지 — 일 단위 기록 (서비스 리포트의 월별 시트 반복 행 대체).
 * 수 주짜리 설치 출장도 같은 AS 건 아래 일지 행으로 누적된다.
 */
@Entity
@Getter
@Table(name = "service_visits",
        indexes = {
                @Index(name = "idx_service_visits_after_service_id", columnList = "after_service_id"),
                @Index(name = "idx_service_visits_engineer_id", columnList = "engineer_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "after_service_id", nullable = false,
            comment = "AS 건 식별자")
    private Long afterServiceId;

    @Column(name = "visit_date", nullable = false,
            comment = "방문일")
    private LocalDate visitDate;

    @Column(name = "engineer_id", nullable = false,
            comment = "담당 엔지니어 (모듈 내부 마스터)")
    private Long engineerId;

    @Column(columnDefinition = "TEXT",
            comment = "문제 (증상 / 발견 사항)")
    private String problem;

    @Column(columnDefinition = "TEXT",
            comment = "해결 (조치 내용)")
    private String resolution;

    @Builder
    ServiceVisit(Long afterServiceId,
                 LocalDate visitDate,
                 Long engineerId,
                 String problem,
                 String resolution) {
        this.afterServiceId = afterServiceId;
        this.visitDate = visitDate;
        this.engineerId = engineerId;
        this.problem = problem;
        this.resolution = resolution;
    }

    public void update(LocalDate visitDate, Long engineerId, String problem, String resolution) {
        this.visitDate = visitDate;
        this.engineerId = engineerId;
        this.problem = problem;
        this.resolution = resolution;
    }
}
