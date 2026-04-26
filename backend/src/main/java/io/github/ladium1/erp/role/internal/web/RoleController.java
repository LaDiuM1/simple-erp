package io.github.ladium1.erp.role.internal.web;

import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.AvailabilityResponse;
import io.github.ladium1.erp.role.internal.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.internal.dto.RoleDetailResponse;
import io.github.ladium1.erp.role.internal.dto.RoleSearchCondition;
import io.github.ladium1.erp.role.internal.dto.RoleSummaryResponse;
import io.github.ladium1.erp.role.internal.dto.RoleUpdateRequest;
import io.github.ladium1.erp.role.internal.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private static final String MENU_CODE = "ROLES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /**
     * 드롭다운 reference — 직원 관리 / 권한 관리 양쪽에서 사용.
     */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, 'EMPLOYEES') "
            + "or @menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<RoleInfo> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_READ)
    public PageResponse<RoleSummaryResponse> search(
            @RequestParam(required = false) String codeKeyword,
            @RequestParam(required = false) String nameKeyword,
            @PageableDefault(sort = "code", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return roleService.search(new RoleSearchCondition(codeKeyword, nameKeyword), pageable);
    }

    @GetMapping("/code-availability")
    @PreAuthorize(CAN_WRITE)
    public AvailabilityResponse checkCodeAvailability(@RequestParam String code) {
        return new AvailabilityResponse(roleService.isCodeAvailable(code));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public RoleDetailResponse getDetail(@PathVariable Long id) {
        return roleService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody RoleCreateRequest request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
