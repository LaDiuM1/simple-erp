package io.github.ladium1.erp.product.internal.init;

import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import io.github.ladium1.erp.product.internal.repository.ProductCategoryRepository;
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
 * 제품 카테고리 seed — 2026 카탈로그의 제품 분류 체계.
 * 최초 1회만 실행 (기존 데이터가 있으면 skip). 이후 분류 변경은 카테고리 관리 화면에서 사용자가 직접.
 */
@Slf4j
@Component
@Order(100)
@ConditionalOnProperty(name = "app.reference-bootstrap.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ProductCategoryInitializer implements ApplicationRunner {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (productCategoryRepository.count() > 0) {
            return;
        }

        log.info("제품 카테고리 seed 시작");
        List<String> names = List.of(
                "평판 레이저",
                "형강 레이저",
                "파이프 레이저",
                "절곡기",
                "복합기",
                "디버링기",
                "엣지머신",
                "용접기",
                "발진기",
                "기타"
        );
        for (int i = 0; i < names.size(); i++) {
            productCategoryRepository.save(ProductCategory.builder()
                    .name(names.get(i))
                    .sortOrder(i + 1)
                    .build());
        }
        log.info("제품 카테고리 seed 완료 — {}건", names.size());
    }
}
