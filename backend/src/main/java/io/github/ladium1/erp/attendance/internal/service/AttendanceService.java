package io.github.ladium1.erp.attendance.internal.service;

import io.github.ladium1.erp.attendance.internal.dto.AttendanceCorrectRequest;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceSearchCondition;
import io.github.ladium1.erp.attendance.internal.dto.CheckInRequest;
import io.github.ladium1.erp.attendance.internal.dto.CheckOutRequest;
import io.github.ladium1.erp.attendance.internal.entity.Attendance;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.repository.AttendanceRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeApi employeeApi;
    private final GeoDistanceCalculator geoDistanceCalculator;

    @Auditable(menu = Menu.ATTENDANCE, action = AuditAction.CREATE, targetType = "Attendance")
    @Transactional
    public AttendanceResponse checkIn(String loginId, CheckInRequest request) {
        EmployeeInfo employee = resolveEmployee(loginId);
        LocalDate today = LocalDate.now();

        // 생성 경로가 check-in 뿐이므로 오늘 행 존재 = 이미 체크인
        if (attendanceRepository.findByEmployeeIdAndWorkDate(employee.id(), today).isPresent()) {
            throw new BusinessException(AttendanceErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.builder()
                .employeeId(employee.id())
                .workDate(today)
                .checkInAt(LocalDateTime.now())
                .checkInLatitude(request.latitude())
                .checkInLongitude(request.longitude())
                .checkInWithinRange(geoDistanceCalculator.isWithinOfficeRange(request.latitude(), request.longitude()))
                .build();

        try {
            // pre-check 통과 후 동시 요청이 먼저 INSERT 한 경합 — flush 로 유니크 위반을 메서드 안에서 잡아 409 변환
            return AttendanceResponse.from(attendanceRepository.saveAndFlush(attendance), employee.name());
        } catch (DataIntegrityViolationException duplicate) {
            throw new BusinessException(AttendanceErrorCode.ALREADY_CHECKED_IN);
        }
    }

    @Auditable(menu = Menu.ATTENDANCE, action = AuditAction.UPDATE, targetType = "Attendance")
    @Transactional
    public AttendanceResponse checkOut(String loginId, CheckOutRequest request) {
        EmployeeInfo employee = resolveEmployee(loginId);
        LocalDate today = LocalDate.now();

        // 자정을 넘긴 퇴근 — 오늘 행이 없으면 어제의 미퇴근 행에 기록
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.id(), today)
                .or(() -> attendanceRepository.findByEmployeeIdAndWorkDateAndCheckOutAtIsNull(
                        employee.id(), today.minusDays(1)))
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.NOT_CHECKED_IN_YET));
        if (attendance.getCheckOutAt() != null) {
            throw new BusinessException(AttendanceErrorCode.ALREADY_CHECKED_OUT);
        }

        attendance.checkOut(
                LocalDateTime.now(),
                request.latitude(),
                request.longitude(),
                geoDistanceCalculator.isWithinOfficeRange(request.latitude(), request.longitude())
        );

        return AttendanceResponse.from(attendance, employee.name());
    }

    /** 관리자 근태 정정 — 시각만 수동 보정, 위치 / withinRange 기록은 유지. */
    @Auditable(menu = Menu.ATTENDANCE, action = AuditAction.UPDATE, targetType = "Attendance", targetIdParam = "id")
    @Transactional
    public AttendanceResponse correct(Long id, AttendanceCorrectRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.correct(request.checkInAt(), request.checkOutAt());

        return AttendanceResponse.from(attendance, employeeApi.getById(attendance.getEmployeeId()).name());
    }

    public List<AttendanceResponse> getMyMonthly(String loginId, int year, int month) {
        EmployeeInfo employee = resolveEmployee(loginId);
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        return attendanceRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employee.id(), monthStart, monthEnd)
                .stream()
                .map(attendance -> AttendanceResponse.from(attendance, employee.name()))
                .toList();
    }

    public PageResponse<AttendanceResponse> search(AttendanceSearchCondition condition, Pageable pageable) {
        Page<Attendance> page = attendanceRepository.search(condition, pageable);

        List<Long> employeeIds = page.getContent().stream()
                .map(Attendance::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, String> employeeNames = employeeApi.findByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeInfo::id, EmployeeInfo::name));

        return PageResponse.of(page.map(
                attendance -> AttendanceResponse.from(attendance, employeeNames.get(attendance.getEmployeeId()))
        ));
    }

    private EmployeeInfo resolveEmployee(String loginId) {
        return employeeApi.findByLoginId(loginId)
                .orElseThrow(() -> new AccessDeniedException("인증된 직원 정보를 찾을 수 없습니다."));
    }
}
