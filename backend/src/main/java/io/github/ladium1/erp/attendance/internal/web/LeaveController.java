package io.github.ladium1.erp.attendance.internal.web;

import io.github.ladium1.erp.attendance.internal.dto.LeaveAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceUpdateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveCreateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;
import io.github.ladium1.erp.attendance.internal.service.LeaveService;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

/**
 * 휴가 / 연차 — 본인 신청 / 조회는 CAN_READ (메뉴 사용자 전원), 관리자 목록 / 잔여 관리는 CAN_WRITE.
 */
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private static final String MENU_CODE = "ATTENDANCE";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize(CAN_READ)
    public Long create(@AuthenticationPrincipal User user,
                       @Valid @RequestBody LeaveCreateRequest request) {
        return leaveService.create(user.getUsername(), request);
    }

    @GetMapping("/me")
    @PreAuthorize(CAN_READ)
    public List<LeaveResponse> getMyLeaves(@AuthenticationPrincipal User user) {
        return leaveService.getMyLeaves(user.getUsername());
    }

    @GetMapping("/balance/me")
    @PreAuthorize(CAN_READ)
    public LeaveBalanceResponse getMyBalance(@AuthenticationPrincipal User user,
                                             @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return leaveService.getMyBalance(user.getUsername(), targetYear);
    }

    /** 관리자 휴가 신청 목록 — 기간 조건은 신청 기간과의 겹침 판정 */
    @GetMapping
    @PreAuthorize(CAN_WRITE)
    public PageResponse<LeaveAdminResponse> search(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return leaveService.search(new LeaveSearchCondition(status, employeeId, startDate, endDate), pageable);
    }

    /** 관리자 잔여 연차 목록 — 재직 직원 전체 기준 */
    @GetMapping("/balances")
    @PreAuthorize(CAN_WRITE)
    public List<LeaveBalanceAdminResponse> getBalances(@RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return leaveService.getBalances(targetYear);
    }

    /** 관리자 부여 일수 조정 */
    @PutMapping("/balances/{employeeId}")
    @PreAuthorize(CAN_WRITE)
    public void changeGrantedDays(@PathVariable Long employeeId,
                                  @Valid @RequestBody LeaveBalanceUpdateRequest request) {
        leaveService.changeGrantedDays(employeeId, request);
    }
}
