package io.github.ladium1.erp.supplier.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.SupplierDeletingEvent;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import io.github.ladium1.erp.supplier.internal.dto.SupplierCreateRequest;
import io.github.ladium1.erp.supplier.internal.dto.SupplierDetailResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSearchCondition;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSummaryResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierUpdateRequest;
import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import io.github.ladium1.erp.supplier.internal.exception.SupplierErrorCode;
import io.github.ladium1.erp.supplier.internal.mapper.SupplierMapper;
import io.github.ladium1.erp.supplier.internal.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService implements SupplierApi {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SupplierInfo getById(Long id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toSupplierInfo)
                .orElseThrow(() -> new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Override
    public List<SupplierInfo> findAll() {
        return supplierMapper.toSupplierInfos(
                supplierRepository.findAll(Sort.by("name").ascending())
        );
    }

    @Override
    public List<SupplierInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return supplierMapper.toSupplierInfos(supplierRepository.findAllById(ids));
    }

    public PageResponse<SupplierSummaryResponse> search(SupplierSearchCondition condition, Pageable pageable) {
        Page<Supplier> page = supplierRepository.search(condition, pageable);
        return PageResponse.of(page.map(supplierMapper::toSummaryResponse));
    }

    public SupplierDetailResponse getDetail(Long id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toDetailResponse)
                .orElseThrow(() -> new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Auditable(menu = Menu.SUPPLIERS, action = AuditAction.CREATE, targetType = "Supplier", targetIdFromReturn = true)
    @Transactional
    public Long create(SupplierCreateRequest request) {
        String name = request.name().trim();
        if (supplierRepository.existsByName(name)) {
            throw new BusinessException(SupplierErrorCode.DUPLICATE_NAME);
        }

        Supplier supplier = Supplier.builder()
                .name(name)
                .nameKo(request.nameKo())
                .country(request.country())
                .note(request.note())
                .active(request.active())
                .build();

        return supplierRepository.save(supplier).getId();
    }

    @Auditable(menu = Menu.SUPPLIERS, action = AuditAction.UPDATE, targetType = "Supplier", targetIdParam = "id")
    @Transactional
    public void update(Long id, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND));

        String name = request.name().trim();
        if (supplierRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException(SupplierErrorCode.DUPLICATE_NAME);
        }

        supplier.update(name, request.nameKo(), request.country(), request.note(), request.active());
    }

    @Auditable(menu = Menu.SUPPLIERS, action = AuditAction.DELETE, targetType = "Supplier", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND);
        }
        // 다른 모듈 (제품 모델 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new SupplierDeletingEvent(id));
        supplierRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.SUPPLIERS, action = AuditAction.DELETE, targetType = "Supplier")
    @Transactional
    public void deleteAll(List<Long> ids) {
        RequestCollectionPolicy.requireBoundedMutationBatch(ids);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }
}
