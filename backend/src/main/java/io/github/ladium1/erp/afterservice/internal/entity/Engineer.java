package io.github.ladium1.erp.afterservice.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 엔지니어 마스터 — AS 에서만 참조되는 모듈 내부 entity (독립 메뉴가 필요할 규모가 아니라
 * AS 관리의 서브 기능으로 관리, 필요가 증명되면 도메인 모듈로 승격).
 * 내부 직원 엔지니어는 employeeId 링크가 optional — 외주 / 제조사 엔지니어는 직원이 아니다.
 */
@Entity
@Getter
@Table(name = "engineers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Engineer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50,
            comment = "이름")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "구분 (내부 / 외주 / 제조사)")
    private EngineerType type;

    @Column(length = 100,
            comment = "소속 (외주 업체명 / 공급사명 등)")
    private String affiliation;

    @Column(length = 30,
            comment = "연락처")
    private String phone;

    @Column(name = "employee_id",
            comment = "내부 직원 링크 (employee 모듈) — 내부 구분일 때만 선택 입력")
    private Long employeeId;

    @Column(nullable = false,
            comment = "사용 여부 — 계약 종료된 외주 등 숨김용")
    private boolean active;

    @Builder
    Engineer(String name,
             EngineerType type,
             String affiliation,
             String phone,
             Long employeeId,
             boolean active) {
        this.name = name;
        this.type = type;
        this.affiliation = affiliation;
        this.phone = phone;
        this.employeeId = employeeId;
        this.active = active;
    }

    public void update(String name,
                       EngineerType type,
                       String affiliation,
                       String phone,
                       Long employeeId,
                       boolean active) {
        this.name = name;
        this.type = type;
        this.affiliation = affiliation;
        this.phone = phone;
        this.employeeId = employeeId;
        this.active = active;
    }
}
