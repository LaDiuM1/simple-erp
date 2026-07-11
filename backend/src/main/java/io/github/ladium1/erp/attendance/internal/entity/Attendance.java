package io.github.ladium1.erp.attendance.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 하루 단위 출퇴근 기록 — check-in 시 생성되고 check-out 으로 갱신된다.
 * <p>
 * 반경 밖 위치는 차단하지 않고 withinRange 로 기록만 한다 (외근 케이스 허용, 화면에서 구분 표시).
 */
@Entity
@Getter
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendances_employee_work_date",
                columnNames = {"employee_id", "work_date"}
        ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            comment = "직원 ID — employee 모듈 참조 (bare Long)")
    private Long employeeId;

    @Column(nullable = false,
            comment = "근무일")
    private LocalDate workDate;

    @Column(comment = "출근 시각")
    private LocalDateTime checkInAt;

    @Column(comment = "퇴근 시각")
    private LocalDateTime checkOutAt;

    @Column(comment = "출근 위치 위도")
    private Double checkInLatitude;

    @Column(comment = "출근 위치 경도")
    private Double checkInLongitude;

    @Column(comment = "퇴근 위치 위도")
    private Double checkOutLatitude;

    @Column(comment = "퇴근 위치 경도")
    private Double checkOutLongitude;

    @Column(nullable = false,
            comment = "출근 위치가 사무실 허용 반경 내인지")
    private boolean checkInWithinRange;

    @Column(nullable = false,
            comment = "퇴근 위치가 사무실 허용 반경 내인지")
    private boolean checkOutWithinRange;

    @Builder
    Attendance(Long employeeId, LocalDate workDate, LocalDateTime checkInAt,
               Double checkInLatitude, Double checkInLongitude, boolean checkInWithinRange) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInAt = checkInAt;
        this.checkInLatitude = checkInLatitude;
        this.checkInLongitude = checkInLongitude;
        this.checkInWithinRange = checkInWithinRange;
    }

    public void checkOut(LocalDateTime checkOutAt, Double latitude, Double longitude, boolean withinRange) {
        this.checkOutAt = checkOutAt;
        this.checkOutLatitude = latitude;
        this.checkOutLongitude = longitude;
        this.checkOutWithinRange = withinRange;
    }

    /** 관리자 수동 정정 — 시각만 보정하고 위치 / withinRange 기록은 유지. null 인 값은 기존 값 유지. */
    public void correct(LocalDateTime checkInAt, LocalDateTime checkOutAt) {
        if (checkInAt != null) {
            this.checkInAt = checkInAt;
        }
        if (checkOutAt != null) {
            this.checkOutAt = checkOutAt;
        }
    }
}
