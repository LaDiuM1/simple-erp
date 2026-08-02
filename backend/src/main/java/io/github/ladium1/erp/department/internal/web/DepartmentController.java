package io.github.ladium1.erp.department.internal.web;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentCreateRequest;
import io.github.ladium1.erp.department.internal.dto.DepartmentDetailResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentSearchCondition;
import io.github.ladium1.erp.department.internal.dto.DepartmentSummaryResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentUpdateRequest;
import io.github.ladium1.erp.department.internal.service.DepartmentService;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private static final String MENU_CODE = "DEPARTMENTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /** 직원 참조 검색의 부서 필터와 부서 선택에서 사용하는 참조 권한. */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, 'EMPLOYEES') "
            + "or @menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'APPROVALS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'EXPENSES') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'ATTENDANCE') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'SALES_CUSTOMERS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'CONTRACTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'AFTER_SERVICES')";

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<DepartmentInfo> findAll() {
        return departmentService.findAll();
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_READ)
    public PageResponse<DepartmentSummaryResponse> search(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @PageableDefault(sort = "code", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return departmentService.search(new DepartmentSearchCondition(codeKeyword, nameKeyword), pageable);
    }

    @GetMapping("/code-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkCodeAvailability(@RequestParam String code) {
        return new AvailabilityResponse(departmentService.isCodeAvailable(code));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public DepartmentDetailResponse getDetail(@PathVariable Long id) {
        return departmentService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody DepartmentCreateRequest request) {
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateRequest request) {
        departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        departmentService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        departmentService.deleteAll(ids);
    }
}
