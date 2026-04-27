package io.github.ladium1.erp.salescontact.internal.web;

import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceInfo;
import io.github.ladium1.erp.salescontact.internal.service.AcquisitionSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 영업 명부의 sub-master 인 컨택 경로 CRUD. SALES_CONTACTS 권한으로 제어.
 * 등록 / 삭제만 — 수정은 텍스트 마스터의 단순성을 유지하기 위해 의도적으로 미제공.
 */
@RestController
@RequestMapping("/api/v1/sales-contacts/acquisition-sources")
@RequiredArgsConstructor
public class AcquisitionSourceController {

    private static final String MENU_CODE = "SALES_CONTACTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final AcquisitionSourceService sourceService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<AcquisitionSourceInfo> findAll() {
        return sourceService.findAll();
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody AcquisitionSourceCreateRequest request) {
        return sourceService.create(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        sourceService.delete(id);
    }
}
