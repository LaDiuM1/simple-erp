package io.github.ladium1.erp.attendance.internal.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 휴가 신청 — 생성 = 즉시 결재 상신. 상태 전이는 결재 결과 콜백 (approve / reject) 으로만.
 */
@Entity
@Getter
@Table(name = "leave_requests",
        indexes = @Index(name = "idx_leave_requests_employee_id", columnList = "employee_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            comment = "신청 직원 ID — employee 모듈 참조 (bare Long)")
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "휴가 유형")
    private LeaveType leaveType;

    @Column(nullable = false,
            comment = "시작일")
    private LocalDate startDate;

    @Column(nullable = false,
            comment = "종료일")
    private LocalDate endDate;

    @Column(nullable = false, precision = 4, scale = 1,
            comment = "사용 일수 — 주말 제외 계산, 반차 0.5")
    private BigDecimal days;

    @Column(length = 500,
            comment = "사유")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "신청 상태")
    private LeaveStatus status;

    @Column(comment = "연동된 결재 문서 ID — approval 모듈 참조 (bare Long)")
    private Long approvalDocumentId;

    @Builder
    LeaveRequest(Long employeeId, LeaveType leaveType, LocalDate startDate, LocalDate endDate,
                 BigDecimal days, String reason) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.reason = reason;
        this.status = LeaveStatus.IN_PROGRESS;
    }

    public void approve() {
        this.status = LeaveStatus.APPROVED;
    }

    public void reject() {
        this.status = LeaveStatus.REJECTED;
    }

    public void linkApprovalDocument(Long approvalDocumentId) {
        this.approvalDocumentId = approvalDocumentId;
    }
}
