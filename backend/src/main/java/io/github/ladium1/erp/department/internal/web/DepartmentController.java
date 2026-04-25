package io.github.ladium1.erp.department.internal.web;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    // 직원 관리 페이지에서 부서 드롭다운/필터로 소비되므로 동일한 메뉴 권한으로 묶는다.
    private static final String MENU_CODE = "EMPLOYEES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final DepartmentApi departmentApi;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<DepartmentInfo> findAll() {
        return departmentApi.findAll();
    }
}
