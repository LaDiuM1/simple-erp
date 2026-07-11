package io.github.ladium1.erp.afterservice.internal.listener;

import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.repository.AfterServiceRepository;
import io.github.ladium1.erp.equipment.api.EquipmentDeletingEvent;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 설비 삭제 시도 시 해당 설비를 참조하는 AS 건이 있는지 검사한다.
 * 동기 EventListener — 예외를 던지면 발행 측 트랜잭션이 롤백되어 삭제가 거부된다.
 */
@Component
@RequiredArgsConstructor
public class EquipmentDeletionListener {

    private final AfterServiceRepository afterServiceRepository;

    @EventListener
    public void on(EquipmentDeletingEvent event) {
        if (afterServiceRepository.existsByEquipmentId(event.equipmentId())) {
            throw new BusinessException(AfterServiceErrorCode.EQUIPMENT_IN_USE);
        }
    }
}
