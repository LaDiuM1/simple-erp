package io.github.ladium1.erp.product.internal.listener;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.repository.ProductRepository;
import io.github.ladium1.erp.supplier.api.SupplierDeletingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 공급사 삭제 시도 시 해당 공급사를 참조하는 제품 모델이 있는지 검사한다.
 * 동기 EventListener — 예외를 던지면 발행 측 트랜잭션이 롤백되어 삭제가 거부된다.
 */
@Component
@RequiredArgsConstructor
public class ProductSupplierDeletionListener {

    private final ProductRepository productRepository;

    @EventListener
    public void on(SupplierDeletingEvent event) {
        if (productRepository.existsBySupplierId(event.supplierId())) {
            throw new BusinessException(ProductErrorCode.SUPPLIER_IN_USE);
        }
    }
}
