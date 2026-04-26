package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeKeyResolverTest {

    private ScopeKeyResolver resolver;

    private final LocalDate FIXED_DATE = LocalDate.of(2026, 4, 26);

    @BeforeEach
    void setUp() {
        resolver = new ScopeKeyResolver();
    }

    @Test
    @DisplayName("NEVER 정책은 항상 GLOBAL")
    void never_policy_returns_global() {
        CodeRule rule = ruleWith(ResetPolicy.NEVER, false);
        assertThat(resolver.resolve(rule, null, FIXED_DATE)).isEqualTo("GLOBAL");
    }

    @Test
    @DisplayName("YEARLY 정책은 연도 4자리")
    void yearly_policy_returns_year() {
        CodeRule rule = ruleWith(ResetPolicy.YEARLY, false);
        assertThat(resolver.resolve(rule, null, FIXED_DATE)).isEqualTo("2026");
    }

    @Test
    @DisplayName("MONTHLY 정책은 연-월")
    void monthly_policy_returns_year_month() {
        CodeRule rule = ruleWith(ResetPolicy.MONTHLY, false);
        assertThat(resolver.resolve(rule, null, FIXED_DATE)).isEqualTo("2026-04");
    }

    @Test
    @DisplayName("DAILY 정책은 연-월-일")
    void daily_policy_returns_year_month_day() {
        CodeRule rule = ruleWith(ResetPolicy.DAILY, false);
        assertThat(resolver.resolve(rule, null, FIXED_DATE)).isEqualTo("2026-04-26");
    }

    @Test
    @DisplayName("parentScoped + parentCode 가 있으면 base 뒤에 |parentCode 결합")
    void parent_scoped_with_parent_appends_parent_code() {
        CodeRule rule = ruleWith(ResetPolicy.YEARLY, true);
        assertThat(resolver.resolve(rule, "D001", FIXED_DATE)).isEqualTo("2026|D001");
    }

    @Test
    @DisplayName("parentScoped 이지만 parentCode 가 null 이면 base 그대로")
    void parent_scoped_without_parent_returns_base_only() {
        CodeRule rule = ruleWith(ResetPolicy.NEVER, true);
        assertThat(resolver.resolve(rule, null, FIXED_DATE)).isEqualTo("GLOBAL");
    }

    @Test
    @DisplayName("parentScoped 가 false 이면 parentCode 가 있어도 base 그대로")
    void non_scoped_ignores_parent() {
        CodeRule rule = ruleWith(ResetPolicy.MONTHLY, false);
        assertThat(resolver.resolve(rule, "D001", FIXED_DATE)).isEqualTo("2026-04");
    }

    private CodeRule ruleWith(ResetPolicy policy, boolean parentScoped) {
        return CodeRule.builder()
                .target(CodeRuleTarget.DEPARTMENT)
                .prefix("D")
                .pattern("{PREFIX}{SEQ:3}")
                .defaultSeqLength(3)
                .resetPolicy(policy)
                .inputMode(InputMode.AUTO)
                .parentScoped(parentScoped)
                .description("test")
                .build();
    }
}
