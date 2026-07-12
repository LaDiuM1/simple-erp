package io.github.ladium1.erp.afterservice.internal.web;

import io.github.ladium1.erp.afterservice.internal.dto.EngineerRequest;
import io.github.ladium1.erp.afterservice.internal.dto.EngineerResponse;
import io.github.ladium1.erp.afterservice.internal.service.EngineerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 엔지니어 마스터 — AS 관리 (AFTER_SERVICES) 의 서브 기능이라 부모 메뉴 권한을 그대로 사용
 * (제품 카테고리가 PRODUCTS 권한을 쓰는 것과 동일 패턴).
 */
@RestController
@RequestMapping("/api/v1/after-services/engineers")
@RequiredArgsConstructor
public class EngineerController {

    private static final String MENU_CODE = "AFTER_SERVICES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final EngineerService engineerService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<EngineerResponse> findAll() {
        return engineerService.findAll();
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody EngineerRequest request) {
        return engineerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody EngineerRequest request) {
        engineerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        engineerService.delete(id);
    }
}
