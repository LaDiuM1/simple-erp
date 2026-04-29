package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.ResetPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 시퀀스 분리 키 결정.
 * <p>
 * 정책은 패턴에서 자동 추론 (PatternCompiler.resolveResetPolicy).
 * 패턴이 {@code {PARENT}} 를 사용하면 부모별로, attribute 토큰을 사용하면 분류별로 시퀀스가 분리된다.
 */
@Component
@RequiredArgsConstructor
public class ScopeKeyResolver {

    private final PatternCompiler patternCompiler;

    public String resolve(String pattern,
                          String parentCode,
                          Map<String, String> sourceAttributes,
                          LocalDate now,
                          Set<String> usedAttributeKeys) {
        ResetPolicy policy = patternCompiler.resolveResetPolicy(pattern);
        boolean usesParent = patternCompiler.usesParentToken(pattern);

        StringBuilder sb = new StringBuilder(timeBase(policy, now));
        if (usesParent && parentCode != null && !parentCode.isBlank()) {
            sb.append("|P=").append(parentCode);
        }
        if (usedAttributeKeys != null && sourceAttributes != null) {
            for (String key : usedAttributeKeys) {
                String src = sourceAttributes.get(key);
                if (src != null && !src.isBlank()) {
                    sb.append("|").append(key).append("=").append(src);
                }
            }
        }
        return sb.toString();
    }

    private String timeBase(ResetPolicy policy, LocalDate now) {
        return switch (policy) {
            case NEVER -> "GLOBAL";
            case YEARLY -> String.format("%04d", now.getYear());
            case MONTHLY -> String.format("%04d-%02d", now.getYear(), now.getMonthValue());
            case DAILY -> String.format("%04d-%02d-%02d",
                    now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        };
    }
}
