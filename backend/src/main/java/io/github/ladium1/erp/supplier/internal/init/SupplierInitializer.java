package io.github.ladium1.erp.supplier.internal.init;

import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import io.github.ladium1.erp.supplier.internal.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공급사 마스터 seed — 실무 엑셀 (설비계약현황 / 설치완료 업체리스트) 에서 추출한 거래 공급사.
 * 최초 1회만 실행 (기존 데이터가 있으면 skip). 한글 표기는 엑셀에서 실사용이 확인된 것만 채움.
 */
@Slf4j
@Component
@Order(100)
@ConditionalOnProperty(name = "app.reference-bootstrap.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SupplierInitializer implements ApplicationRunner {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (supplierRepository.count() > 0) {
            return;
        }

        log.info("공급사 마스터 seed 시작");
        List<Supplier> suppliers = List.of(
                seed("YAWEI", "야웨이"),
                seed("HG TECH", "HG테크"),
                seed("BAISHENG", "바이셩"),
                seed("ADH", null),
                seed("KEBEI", null),
                seed("ACME", null),
                seed("LCM", null),
                seed("NITIAN", null),
                seed("JQ", null),
                seed("YX", null)
        );
        supplierRepository.saveAll(suppliers);
        log.info("공급사 마스터 seed 완료 — {}건", suppliers.size());
    }

    private Supplier seed(String name, String nameKo) {
        return Supplier.builder()
                .name(name)
                .nameKo(nameKo)
                .country("중국")
                .active(true)
                .build();
    }
}
