package io.github.ladium1.erp.coderule.internal.init;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
@RequiredArgsConstructor
public class CodeRuleInitializer implements ApplicationRunner {

    private static final Map<CodeRuleTarget, CodeRule> DEFAULTS = new EnumMap<>(CodeRuleTarget.class);

    static {
        DEFAULTS.put(CodeRuleTarget.DEPARTMENT, CodeRule.builder()
                .target(CodeRuleTarget.DEPARTMENT)
                .prefix("D")
                .pattern("{PREFIX}{SEQ:3}")
                .defaultSeqLength(3)
                .resetPolicy(ResetPolicy.NEVER)
                .inputMode(InputMode.AUTO_OR_MANUAL)
                .parentScoped(false)
                .description("부서 코드 — 기본: D001, D002, ...")
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
                    .prefix(template.getPrefix())
                    .pattern(template.getPattern())
                    .defaultSeqLength(template.getDefaultSeqLength())
                    .resetPolicy(template.getResetPolicy())
                    .inputMode(template.getInputMode())
                    .parentScoped(template.isParentScoped())
                    .description(template.getDescription())
                    .build());
            log.info("기본 채번 규칙 생성: {}", target);
        }
    }
}
