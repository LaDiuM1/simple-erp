package io.github.ladium1.erp.equipment.internal.web;

import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentDetailResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentReferenceResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSummaryResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.equipment.internal.dto.WarrantyFilter;
import io.github.ladium1.erp.equipment.internal.service.EquipmentService;
import io.github.ladium1.erp.global.web.DownloadResponse;
import io.github.ladium1.erp.global.web.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private static final String MENU_CODE = "EQUIPMENTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    /** AS 접수에서 고객사 범위의 설비 선택과 보증 판단에 사용하는 참조 권한. */
    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'AFTER_SERVICES')";

    private final EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public PageResponse<EquipmentSummaryResponse> search(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String serialKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) WarrantyFilter warranty,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return equipmentService.search(
                new EquipmentSearchCondition(customerId, supplierId, serialKeyword, addressKeyword, warranty),
                pageable
        );
    }

    @GetMapping("/reference")
    @PreAuthorize(CAN_READ_REFERENCE)
    public PageResponse<EquipmentReferenceResponse> searchReference(
            @RequestParam Long customerId,
            @RequestParam(required = false) String serialKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) WarrantyFilter warranty,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return equipmentService.searchReference(
                new EquipmentSearchCondition(customerId, null, serialKeyword, addressKeyword, warranty),
                pageable
        );
    }

    @GetMapping("/reference/{id}")
    @PreAuthorize(CAN_READ_REFERENCE)
    public EquipmentReferenceResponse getReference(
            @PathVariable Long id,
            @RequestParam Long customerId
    ) {
        return equipmentService.getReference(id, customerId);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public EquipmentDetailResponse getDetail(@PathVariable Long id) {
        return equipmentService.getDetail(id);
    }

    @GetMapping("/excel")
    @PreAuthorize(CAN_READ)
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String serialKeyword,
            @RequestParam(required = false) String addressKeyword,
            @RequestParam(required = false) WarrantyFilter warranty,
            @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort
    ) {
        byte[] bytes = equipmentService.exportExcel(
                new EquipmentSearchCondition(customerId, supplierId, serialKeyword, addressKeyword, warranty),
                sort
        );
        String filename = "equipments_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        return DownloadResponse.xlsx(bytes, filename);
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody EquipmentCreateRequest request) {
        return equipmentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody EquipmentUpdateRequest request) {
        equipmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }

    @DeleteMapping
    @PreAuthorize(CAN_WRITE)
    public void deleteAll(@RequestBody List<Long> ids) {
        equipmentService.deleteAll(ids);
    }
}
