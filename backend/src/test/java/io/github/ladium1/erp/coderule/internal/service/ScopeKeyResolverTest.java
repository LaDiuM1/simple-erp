package io.github.ladium1.erp.coderule.internal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeKeyResolverTest {

    private ScopeKeyResolver resolver;
    private final LocalDate FIXED_DATE = LocalDate.of(2026, 4, 26);

    @BeforeEach
    void setUp() {
        resolver = new ScopeKeyResolver(new PatternCompiler());
    }

    @Test
    @DisplayName("날짜 토큰 없음 = NEVER -> GLOBAL")
    void no_date_token_returns_global() {
        assertThat(resolver.resolve("D{SEQ:3}", null, Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("GLOBAL");
    }

    @Test
    @DisplayName("YYYY 토큰 = YEARLY -> 연도 4자리")
    void yearly_returns_year() {
        assertThat(resolver.resolve("{YYYY}-{SEQ:3}", null, Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("2026");
    }

    @Test
    @DisplayName("MM 토큰 포함 = MONTHLY -> 연-월")
    void monthly_returns_year_month() {
        assertThat(resolver.resolve("{YY}{MM}-{SEQ:3}", null, Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("2026-04");
    }

    @Test
    @DisplayName("DD 토큰 포함 = DAILY -> 연-월-일")
    void daily_returns_year_month_day() {
        assertThat(resolver.resolve("{YY}{MM}{DD}-{SEQ:3}", null, Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("2026-04-26");
    }

    @Test
    @DisplayName("PARENT 토큰 사용 + parentCode 가 있으면 base 뒤에 |P=... 결합")
    void parent_token_with_parent_appends() {
        assertThat(resolver.resolve("{PARENT}-{SEQ:3}", "D001", Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("GLOBAL|P=D001");
    }

    @Test
    @DisplayName("PARENT 토큰 미사용 시 parentCode 가 있어도 base 그대로")
    void no_parent_token_ignores_parent() {
        assertThat(resolver.resolve("D{SEQ:3}", "D001", Map.of(), FIXED_DATE, Set.of()))
                .isEqualTo("GLOBAL");
    }

    @Test
    @DisplayName("attribute 토큰 사용 시 base 뒤에 |KEY=sourceValue 결합")
    void attribute_appends() {
        assertThat(resolver.resolve("{TYPE}-{SEQ:3}", null, Map.of("TYPE", "GENERAL"),
                FIXED_DATE, Set.of("TYPE")))
                .isEqualTo("GLOBAL|TYPE=GENERAL");
    }

    @Test
    @DisplayName("YYYY + PARENT + TYPE 조합")
    void all_combine() {
        assertThat(resolver.resolve("{YYYY}-{PARENT}-{TYPE}-{SEQ:3}",
                "D001", Map.of("TYPE", "G"), FIXED_DATE, Set.of("TYPE")))
                .isEqualTo("2026|P=D001|TYPE=G");
    }
}
