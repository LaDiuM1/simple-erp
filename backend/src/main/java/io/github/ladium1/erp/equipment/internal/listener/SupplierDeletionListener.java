package io.github.ladium1.erp.equipment.internal.listener;

import io.github.ladium1.erp.equipment.internal.exception.EquipmentErrorCode;
import io.github.ladium1.erp.equipment.internal.repository.EquipmentRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.supplier.api.SupplierDeletingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 공급사 삭제 시도 시 해당 공급사를 참조하는 설비 대장이 있는지 검사한다.
 * 대장의 supplierId 는 등록 시점 스냅샷이라 제품 모듈 가드만으로는 참조가 남을 수 있어 직접 가드.
 */
@Component
@RequiredArgsConstructor
public class SupplierDeletionListener {

    private final EquipmentRepository equipmentRepository;

    @EventListener
    public void on(SupplierDeletingEvent event) {
        if (equipmentRepository.existsBySupplierId(event.supplierId())) {
            throw new BusinessException(EquipmentErrorCode.SUPPLIER_IN_USE);
        }
    }
}
