package io.github.ladium1.erp.salescustomer.internal.entity;

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
 * 고객사별 영업 담당자 배정 + 이력.
 * <p>
 * endDate == null 이면 현재 활성 배정. 채워지면 종료된 이력 (이직 / 퇴사 / 일반 변경 등) 으로 자연 누적.
 * 별도 history 테이블은 두지 않는다.
 */
@Entity
@Getter
@Table(name = "sales_assignments",
        indexes = {
                @Index(name = "idx_sales_assignments_customer_id", columnList = "customer_id"),
                @Index(name = "idx_sales_assignments_employee_id", columnList = "employee_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, comment = "고객사 식별자")
    private Long customerId;

    @Column(name = "employee_id", nullable = false, comment = "영업 담당 직원 식별자")
    private Long employeeId;

    @Column(name = "start_date", nullable = false, comment = "배정 시작일")
    private LocalDate startDate;

    @Column(name = "end_date", comment = "배정 종료일 — null 이면 현재 담당")
    private LocalDate endDate;

    @Column(name = "is_primary", nullable = false, comment = "주 담당 여부")
    private boolean primary;

    @Column(comment = "배정 / 변경 사유 (이직, 퇴사, 일반 변경 등 자유)")
    private String reason;

    @Builder
    SalesAssignment(Long customerId,
                    Long employeeId,
                    LocalDate startDate,
                    LocalDate endDate,
                    boolean primary,
                    String reason) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.primary = primary;
        this.reason = reason;
    }

    public void update(LocalDate startDate, boolean primary, String reason) {
        this.startDate = startDate;
        this.primary = primary;
        this.reason = reason;
    }

    public void terminate(LocalDate endDate, String reason) {
        this.endDate = endDate;
        this.reason = reason;
        this.primary = false;
    }

    /**
     * 같은 고객사에 새로운 primary 배정이 들어올 때 기존 primary 해제용.
     */
    public void clearPrimary() {
        this.primary = false;
    }

    public boolean isActive() {
        return endDate == null;
    }
}
