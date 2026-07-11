package io.github.ladium1.erp.attendance.internal.web;

import io.github.ladium1.erp.attendance.internal.dto.AttendanceCorrectRequest;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceSearchCondition;
import io.github.ladium1.erp.attendance.internal.dto.CheckInRequest;
import io.github.ladium1.erp.attendance.internal.dto.CheckOutRequest;
import io.github.ladium1.erp.attendance.internal.service.AttendanceService;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private static final String MENU_CODE = "ATTENDANCE";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final AttendanceService attendanceService;

    /** 본인 출퇴근은 메뉴 사용자 전원이 가능해야 하므로 CAN_READ */
    @PostMapping("/check-in")
    @PreAuthorize(CAN_READ)
    public AttendanceResponse checkIn(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody CheckInRequest request) {
        return attendanceService.checkIn(user.getUsername(), request);
    }

    @PostMapping("/check-out")
    @PreAuthorize(CAN_READ)
    public AttendanceResponse checkOut(@AuthenticationPrincipal User user,
                                       @Valid @RequestBody CheckOutRequest request) {
        return attendanceService.checkOut(user.getUsername(), request);
    }

    @GetMapping("/me")
    @PreAuthorize(CAN_READ)
    public List<AttendanceResponse> getMyMonthly(@AuthenticationPrincipal User user,
                                                 @RequestParam int year,
                                                 @RequestParam int month) {
        return attendanceService.getMyMonthly(user.getUsername(), year, month);
    }

    /** 전 직원 근태 현황 — 관리자 화면이므로 CAN_WRITE */
    @GetMapping
    @PreAuthorize(CAN_WRITE)
    public PageResponse<AttendanceResponse> search(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long employeeId,
            @PageableDefault(sort = "workDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return attendanceService.search(new AttendanceSearchCondition(year, month, employeeId), pageable);
    }

    /** 근태 수동 정정 — 관리자 기능이므로 CAN_WRITE */
    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public AttendanceResponse correct(@PathVariable Long id,
                                      @Valid @RequestBody AttendanceCorrectRequest request) {
        return attendanceService.correct(id, request);
    }
}
