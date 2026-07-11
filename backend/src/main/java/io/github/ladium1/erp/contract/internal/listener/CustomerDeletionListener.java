package io.github.ladium1.erp.contract.internal.listener;

import io.github.ladium1.erp.contract.internal.exception.ContractErrorCode;
import io.github.ladium1.erp.contract.internal.repository.ContractRepository;
import io.github.ladium1.erp.customer.api.CustomerDeletingEvent;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 고객사 삭제 시도 시 해당 고객사를 참조하는 계약이 있는지 검사한다.
 * 동기 EventListener — 예외를 던지면 발행 측 트랜잭션이 롤백되어 삭제가 거부된다.
 */
@Component
@RequiredArgsConstructor
public class CustomerDeletionListener {

    private final ContractRepository contractRepository;

    @EventListener
    public void on(CustomerDeletingEvent event) {
        if (contractRepository.existsByCustomerId(event.customerId())) {
            throw new BusinessException(ContractErrorCode.CUSTOMER_IN_USE);
        }
    }
}
