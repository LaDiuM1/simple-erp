package io.github.ladium1.erp.equipment.internal.listener;

import io.github.ladium1.erp.contract.api.ContractInstalledEvent;
import io.github.ladium1.erp.equipment.internal.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 계약 설치완료 (INSTALLED) 전이를 수신해 설비 대장을 자동 생성한다.
 * <p>
 * {@code @ApplicationModuleListener} — 발행 트랜잭션 커밋 후 별도 트랜잭션에서 비동기 처리
 * (event publication registry 에 영속되어 유실 없이 재시도 가능). contract 는 equipment 를 모른다.
 */
@Component
@RequiredArgsConstructor
public class ContractInstalledListener {

    private final EquipmentService equipmentService;

    @ApplicationModuleListener
    public void on(ContractInstalledEvent event) {
        equipmentService.registerFromContract(event);
    }
}
