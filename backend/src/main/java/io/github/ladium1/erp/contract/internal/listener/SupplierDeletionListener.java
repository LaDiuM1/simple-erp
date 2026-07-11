package io.github.ladium1.erp.contract.internal.listener;

import io.github.ladium1.erp.contract.internal.exception.ContractErrorCode;
import io.github.ladium1.erp.contract.internal.repository.ContractRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.supplier.api.SupplierDeletingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 공급사 삭제 시도 시 해당 공급사를 참조하는 계약이 있는지 검사한다.
 * 계약의 supplierId 는 계약 시점 스냅샷이라, 이후 제품 마스터의 공급사가 바뀌면
 * 제품 모듈의 가드만으로는 계약 참조가 남을 수 있어 계약 모듈이 직접 가드한다.
 */
@Component
@RequiredArgsConstructor
public class SupplierDeletionListener {

    private final ContractRepository contractRepository;

    @EventListener
    public void on(SupplierDeletingEvent event) {
        if (contractRepository.existsBySupplierId(event.supplierId())) {
            throw new BusinessException(ContractErrorCode.SUPPLIER_IN_USE);
        }
    }
}
