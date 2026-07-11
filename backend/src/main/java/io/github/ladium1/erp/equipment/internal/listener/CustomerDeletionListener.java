package io.github.ladium1.erp.equipment.internal.listener;

import io.github.ladium1.erp.customer.api.CustomerDeletingEvent;
import io.github.ladium1.erp.equipment.internal.exception.EquipmentErrorCode;
import io.github.ladium1.erp.equipment.internal.repository.EquipmentRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 고객사 삭제 시도 시 해당 고객사를 참조하는 설비 대장이 있는지 검사한다.
 * 동기 EventListener — 예외를 던지면 발행 측 트랜잭션이 롤백되어 삭제가 거부된다.
 */
@Component
@RequiredArgsConstructor
public class CustomerDeletionListener {

    private final EquipmentRepository equipmentRepository;

    @EventListener
    public void on(CustomerDeletingEvent event) {
        if (equipmentRepository.existsByCustomerId(event.customerId())) {
            throw new BusinessException(EquipmentErrorCode.CUSTOMER_IN_USE);
        }
    }
}
