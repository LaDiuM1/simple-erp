package io.github.ladium1.erp.supplier.internal.web;

import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import io.github.ladium1.erp.supplier.internal.dto.SupplierCreateRequest;
import io.github.ladium1.erp.supplier.internal.dto.SupplierDetailResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSearchCondition;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSummaryResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierUpdateRequest;
import io.github.ladium1.erp.supplier.internal.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private static final String MENU_CODE = "SUPPLIERS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /**
     * 드롭다운 / 필터로 소비되는 reference 목록 — 제품 모델 관리 / 공급사 관리 양쪽에서 사용.
     * 둘 중 하나라도 read 권한이 있으면 허용.
     */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, 'PRODUCTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<SupplierInfo> findAll() {
        return supplierService.findAll();
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_READ)
    public PageResponse<SupplierSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return supplierService.search(new SupplierSearchCondition(keyword, active), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public SupplierDetailResponse getDetail(@PathVariable Long id) {
        return supplierService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody SupplierCreateRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        supplierService.deleteAll(ids);
    }
}
