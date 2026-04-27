package io.github.ladium1.erp.salescontact.internal.init;

import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSource;
import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSourceType;
import io.github.ladium1.erp.salescontact.internal.repository.AcquisitionSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 실행 시 컨택 경로 마스터 시드 — row 가 하나도 없을 때만 실행.
 * 사용자가 명함 / 자료 출처로 관리해 온 항목을 분류해 미리 등록한다.
 */
@Slf4j
@Component
@Order(100) // SalesContactInitializer 보다 먼저 실행되도록 명시적 순서 — 동일 모듈 내 sub-master 가 먼저 채워져야 명부 시드의 컨택 경로 매핑이 동작.
@RequiredArgsConstructor
public class AcquisitionSourceInitializer implements ApplicationRunner {

    private final AcquisitionSourceRepository sourceRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (sourceRepository.count() > 0) {
            return;
        }

        log.info("컨택 경로 마스터 시드 초기화 시작");

        save("SIMTOS26", AcquisitionSourceType.EXHIBITION, "서울국제생산제조기술전 2026");
        save("SIMTOS24", AcquisitionSourceType.EXHIBITION, "서울국제생산제조기술전 2024");
        save("DAMEX 2025", AcquisitionSourceType.EXHIBITION, "대구국제기계산업대전 2025");
        save("DAMES25", AcquisitionSourceType.EXHIBITION, "대구국제기계산업대전 2025 (DAMES)");
        save("EXBUTECH25", AcquisitionSourceType.EXHIBITION, "부산 국제 첨단가공기술 박람회 2025");
        save("BUTECH23", AcquisitionSourceType.EXHIBITION, "부산국제기계대전 2023");
        save("CECO23", AcquisitionSourceType.EXHIBITION, "창원컨벤션센터 전시 2023");
        save("CECO24", AcquisitionSourceType.EXHIBITION, "창원컨벤션센터 전시 2024");
        save("MARINE23", AcquisitionSourceType.EXHIBITION, "국제해양·조선·항만 전시 2023");
        save("KADEX24", AcquisitionSourceType.EXHIBITION, "대한민국 방위산업전 2024");
        save("KIMEX24", AcquisitionSourceType.EXHIBITION, "한국국제기계박람회 2024");
        save("METAL24", AcquisitionSourceType.EXHIBITION, "국제 금속 / 비철금속 산업전 2024");
        save("HARF24", AcquisitionSourceType.EXHIBITION, "한국국제 항공우주·로봇 전시 2024");

        save("김창훈부사장명함", AcquisitionSourceType.REFERRAL, "김창훈 부사장님께 받은 명함 / 소개");
        save("이세명대표님명함", AcquisitionSourceType.REFERRAL, "이세명 대표님께 받은 명함 / 소개");

        save("인터넷조사", AcquisitionSourceType.WEB, "직접 인터넷 검색 / 홈페이지 문의");

        save("납품업체", AcquisitionSourceType.OTHER, "기존 납품업체 / 협력사 라인");

        log.info("컨택 경로 마스터 시드 {}건 생성 완료", sourceRepository.count());
    }

    private void save(String name, AcquisitionSourceType type, String description) {
        sourceRepository.save(AcquisitionSource.builder()
                .name(name)
                .type(type)
                .description(description)
                .build());
    }
}
