package io.github.ladium1.erp.coderule.internal.init;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * 채번 규칙 기본값 seed.
 * <p>
 * CodeRuleTarget enum 에 새 상수를 추가하면 {@link #DEFAULTS} 에도 한 줄 추가한다.
 * 기존 규칙은 덮어쓰지 않는다 — 운영 데이터 보호 목적.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class CodeRuleInitializer implements ApplicationRunner {

    private static final Map<CodeRuleTarget, CodeRule> DEFAULTS = new EnumMap<>(CodeRuleTarget.class);

    static {
        DEFAULTS.put(CodeRuleTarget.DEPARTMENT, CodeRule.builder()
                .target(CodeRuleTarget.DEPARTMENT)
                .pattern("D{SEQ:3}")
                .inputMode(InputMode.AUTO_OR_MANUAL)
                .description("부서 코드 — 기본: D001, D002, ...")
                .build());

        DEFAULTS.put(CodeRuleTarget.POSITION, CodeRule.builder()
                .target(CodeRuleTarget.POSITION)
                .pattern("P{SEQ:3}")
                .inputMode(InputMode.AUTO_OR_MANUAL)
                .description("직책 코드 — 기본: P001, P002, ...")
                .build());

        DEFAULTS.put(CodeRuleTarget.CUSTOMER, CodeRule.builder()
                .target(CodeRuleTarget.CUSTOMER)
                .pattern("C{SEQ:4}")
                .inputMode(InputMode.AUTO_OR_MANUAL)
                .description("고객사 코드 — 기본: C0001, C0002, ...")
                .build());

        // 연도 토큰 포함 -> 시퀀스 매년 초기화. 기존 엑셀의 연도별 시트 관리 관행과 동일한 번호 체계.
        DEFAULTS.put(CodeRuleTarget.CONTRACT, CodeRule.builder()
                .target(CodeRuleTarget.CONTRACT)
                .pattern("CT{YYYY}-{SEQ:3}")
                .inputMode(InputMode.AUTO)
                .description("계약 번호 — 기본: CT2026-001, CT2026-002, ... (매년 초기화)")
                .build());

        DEFAULTS.put(CodeRuleTarget.AFTER_SERVICE, CodeRule.builder()
                .target(CodeRuleTarget.AFTER_SERVICE)
                .pattern("AS{YYYY}-{SEQ:4}")
                .inputMode(InputMode.AUTO)
                .description("AS 접수번호 — 기본: AS2026-0001, AS2026-0002, ... (매년 초기화)")
                .build());
    }

    private final CodeRuleRepository codeRuleRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        for (CodeRuleTarget target : CodeRuleTarget.values()) {
            if (codeRuleRepository.existsByTarget(target)) {
                continue;
            }
            CodeRule template = DEFAULTS.get(target);
            if (template == null) {
                log.warn("CodeRuleInitializer: {} 에 대한 기본 규칙이 정의되지 않음", target);
                continue;
            }
            codeRuleRepository.save(CodeRule.builder()
                    .target(template.getTarget())
                    .pattern(template.getPattern())
                    .inputMode(template.getInputMode())
                    .description(template.getDescription())
                    .build());
            log.info("기본 채번 규칙 생성: {}", target);
        }
    }
}
