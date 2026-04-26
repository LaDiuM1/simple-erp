package io.github.ladium1.erp.salescustomer.internal.entity;

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

import java.time.LocalDateTime;

/**
 * 고객사 영업 활동 이력.
 * <p>
 * customerId / ourEmployeeId / customerContactId 는 모듈 경계를 넘는 참조라 bare Long 컬럼으로만 보유.
 * 무결성 검증은 service 레이어에서 CustomerApi / EmployeeApi / SalesContactApi 로.
 * <p>
 * customerContactId 가 있으면 영업 명부 모듈의 SalesContact 와 연결, 없으면 customerContactName / customerContactPosition 자유 입력 fallback.
 */
@Entity
@Getter
@Table(name = "sales_activities",
        indexes = {
                @Index(name = "idx_sales_activities_customer_id", columnList = "customer_id"),
                @Index(name = "idx_sales_activities_activity_date", columnList = "activity_date")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, comment = "고객사 식별자")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "활동 유형")
    private SalesActivityType type;

    @Column(name = "activity_date", nullable = false, comment = "활동 일시")
    private LocalDateTime activityDate;

    @Column(nullable = false, comment = "활동 제목")
    private String subject;

    @Column(columnDefinition = "TEXT", comment = "활동 내용")
    private String content;

    @Column(name = "our_employee_id", nullable = false, comment = "우리쪽 담당 직원 식별자")
    private Long ourEmployeeId;

    @Column(name = "customer_contact_id", comment = "고객사 담당자 (영업 명부 식별자) — 자유 입력 fallback 가능")
    private Long customerContactId;

    @Column(name = "customer_contact_name", comment = "고객사 담당자 이름 (자유 입력 — customerContactId 없을 때 fallback)")
    private String customerContactName;

    @Column(name = "customer_contact_position", comment = "고객사 담당자 직책 / 부서 (자유 입력)")
    private String customerContactPosition;

    @Builder
    SalesActivity(Long customerId,
                  SalesActivityType type,
                  LocalDateTime activityDate,
                  String subject,
                  String content,
                  Long ourEmployeeId,
                  Long customerContactId,
                  String customerContactName,
                  String customerContactPosition) {
        this.customerId = customerId;
        this.type = type;
        this.activityDate = activityDate;
        this.subject = subject;
        this.content = content;
        this.ourEmployeeId = ourEmployeeId;
        this.customerContactId = customerContactId;
        this.customerContactName = customerContactName;
        this.customerContactPosition = customerContactPosition;
    }

    public void update(SalesActivityType type,
                       LocalDateTime activityDate,
                       String subject,
                       String content,
                       Long ourEmployeeId,
                       Long customerContactId,
                       String customerContactName,
                       String customerContactPosition) {
        this.type = type;
        this.activityDate = activityDate;
        this.subject = subject;
        this.content = content;
        this.ourEmployeeId = ourEmployeeId;
        this.customerContactId = customerContactId;
        this.customerContactName = customerContactName;
        this.customerContactPosition = customerContactPosition;
    }
}
