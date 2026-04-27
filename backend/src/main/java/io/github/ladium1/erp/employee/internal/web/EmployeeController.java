package io.github.ladium1.erp.employee.internal.web;

import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.employee.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeCreateRequest;
import io.github.ladium1.erp.employee.internal.dto.EmployeeDetailResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeUpdateRequest;
import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import io.github.ladium1.erp.employee.internal.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private static final String MENU_CODE = "EMPLOYEES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final EmployeeService employeeService;

    @GetMapping("/me")
    public EmployeeProfileResponse getMyInfo(@AuthenticationPrincipal User user) {
        return employeeService.getMyInfo(user.getUsername());
    }

    @GetMapping("/availability")
    @PreAuthorize(CAN_READ)
    public AvailabilityResponse checkLoginIdAvailability(@RequestParam String loginId) {
        return new AvailabilityResponse(employeeService.isLoginIdAvailable(loginId));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<EmployeeSummaryResponse> search(
            @RequestParam(required = false) String loginIdKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) EmployeeStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return employeeService.search(toCondition(loginIdKeyword, nameKeyword, departmentId, positionId, roleId, status), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public EmployeeDetailResponse getDetail(@PathVariable Long id) {
        return employeeService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) String loginIdKeyword,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) EmployeeStatus status,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = employeeService.exportExcel(
                toCondition(loginIdKeyword, nameKeyword, departmentId, positionId, roleId, status),
                sort
        );
        String filename = "employees_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        employeeService.deleteAll(ids);
    }

    private EmployeeSearchCondition toCondition(String loginIdKeyword, String nameKeyword, Long departmentId, Long positionId, Long roleId, EmployeeStatus status) {
        return new EmployeeSearchCondition(loginIdKeyword, nameKeyword, departmentId, positionId, roleId, status);
    }
}
