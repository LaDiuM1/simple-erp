package io.github.ladium1.erp.position.internal.web;

import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    // 직원 관리 페이지에서 직책 드롭다운/필터로 소비되므로 동일한 메뉴 권한으로 묶는다.
    private static final String MENU_CODE = "MDM_HRM";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final PositionApi positionApi;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<PositionInfo> findAll() {
        return positionApi.findAll();
    }
}
