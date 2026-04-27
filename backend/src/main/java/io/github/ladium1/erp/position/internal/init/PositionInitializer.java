package io.github.ladium1.erp.position.internal.init;

import io.github.ladium1.erp.position.internal.entity.Position;
import io.github.ladium1.erp.position.internal.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 실행 시 직책 테스트 데이터 시드.
 * 직책이 하나도 없을 때만 일반적인 사무직 서열 6단계를 생성한다.
 */
@Slf4j
@Component
@Order(100) // 의존 없음 — Employee (Order=200) 의 선결.
@RequiredArgsConstructor
public class PositionInitializer implements ApplicationRunner {

    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (positionRepository.count() > 0) {
            return;
        }

        log.info("직책 테스트 데이터 초기화 시작");

        save("P001", "이사", 1);
        save("P002", "부장", 2);
        save("P003", "차장", 3);
        save("P004", "과장", 4);
        save("P005", "대리", 5);
        save("P006", "사원", 6);

        log.info("직책 테스트 데이터 6건 생성 완료");
    }

    private void save(String code, String name, int rankLevel) {
        positionRepository.save(
                Position.builder()
                        .code(code)
                        .name(name)
                        .rankLevel(rankLevel)
                        .build()
        );
    }
}
