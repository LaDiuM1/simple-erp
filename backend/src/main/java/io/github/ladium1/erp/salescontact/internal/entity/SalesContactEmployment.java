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

import java.time.LocalDate;

/**
 * 영업 명부의 재직 이력.
 * <p>
 * customerId / externalCompanyName 둘 중 하나만 채워짐 — 우리 등록된 고객사이거나 외부 회사 자유 입력.
 * endDate == null 이면 현재 재직, 채워지면 종료된 이력으로 자연 누적 (별도 history 테이블 미보유).
 */
@Entity
@Getter
@Table(name = "sales_contact_employments",
        indexes = {
                @Index(name = "idx_sales_contact_employments_contact_id", columnList = "contact_id"),
                @Index(name = "idx_sales_contact_employments_customer_id", columnList = "customer_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesContactEmployment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_id", nullable = false, comment = "영업 명부 식별자")
    private Long contactId;

    @Column(name = "customer_id", comment = "우리 고객사 식별자 — null 이면 외부 회사")
    private Long customerId;

    @Column(name = "external_company_name", comment = "외부 회사 자유 입력 — customerId 가 null 일 때 채움")
    private String externalCompanyName;

    @Column(comment = "직책")
    private String position;

    @Column(comment = "부서")
    private String department;

    @Column(name = "start_date", nullable = false, comment = "재직 시작일")
    private LocalDate startDate;

    @Column(name = "end_date", comment = "재직 종료일 — null 이면 현재 재직")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_type", comment = "종료 분류 — endDate 가 채워질 때만 의미 있음")
    private DepartureType departureType;

    @Column(name = "departure_note", comment = "종료 사유 자유 메모")
    private String departureNote;

    @Builder
    SalesContactEmployment(Long contactId,
                           Long customerId,
                           String externalCompanyName,
                           String position,
                           String department,
                           LocalDate startDate,
                           LocalDate endDate,
                           DepartureType departureType,
                           String departureNote) {
        this.contactId = contactId;
        this.customerId = customerId;
        this.externalCompanyName = externalCompanyName;
        this.position = position;
        this.department = department;
        this.startDate = startDate;
        this.endDate = endDate;
        this.departureType = departureType;
        this.departureNote = departureNote;
    }

    public void update(Long customerId,
                       String externalCompanyName,
                       String position,
                       String department,
                       LocalDate startDate) {
        this.customerId = customerId;
        this.externalCompanyName = externalCompanyName;
        this.position = position;
        this.department = department;
        this.startDate = startDate;
    }

    public void terminate(LocalDate endDate, DepartureType departureType, String departureNote) {
        this.endDate = endDate;
        this.departureType = departureType;
        this.departureNote = departureNote;
    }

    public boolean isActive() {
        return endDate == null;
    }
}
