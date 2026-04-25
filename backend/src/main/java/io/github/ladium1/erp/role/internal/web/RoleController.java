package io.github.ladium1.erp.role.internal.web;

import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    // 직원 관리 페이지에서 권한 드롭다운/필터로 소비되므로 동일한 메뉴 권한으로 묶는다.
    private static final String MENU_CODE = "EMPLOYEES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final RoleApi roleApi;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<RoleInfo> findAll() {
        return roleApi.findAll();
    }
}
