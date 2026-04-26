package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 시퀀스 분리 키 결정.
 * <p>
 * resetPolicy 가 시간 경계를, parentScoped 가 부모 범위를 분리한다.
 */
@Component
public class ScopeKeyResolver {

    public String resolve(CodeRule rule, String parentCode, LocalDate now) {
        return resolve(rule.getResetPolicy(), rule.isParentScoped(), parentCode, now);
    }

    public String resolve(ResetPolicy policy, boolean parentScoped, String parentCode, LocalDate now) {
        String base = switch (policy) {
            case NEVER -> "GLOBAL";
            case YEARLY -> String.format("%04d", now.getYear());
            case MONTHLY -> String.format("%04d-%02d", now.getYear(), now.getMonthValue());
            case DAILY -> String.format("%04d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        };
        if (parentScoped && parentCode != null && !parentCode.isBlank()) {
            return base + "|" + parentCode;
        }
        return base;
    }
}
