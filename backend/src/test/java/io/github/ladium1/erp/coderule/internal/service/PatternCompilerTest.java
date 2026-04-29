package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.service.PatternCompiler.RenderContext;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatternCompilerTest {

    private PatternCompiler compiler;
    private final LocalDate FIXED_DATE = LocalDate.of(2026, 4, 26);

    @BeforeEach
    void setUp() {
        compiler = new PatternCompiler();
    }

    @Test
    @DisplayName("패턴 검증 — 유효한 토큰 조합 통과")
    void validate_pattern_success() {
        compiler.validate("D{SEQ:3}", Set.of());
        compiler.validate("EMP-{YYYY}-{SEQ:4}", Set.of());
        compiler.validate("{YY}{MM}{DD}-{SEQ:5}", Set.of());
        compiler.validate("{PARENT}-{SEQ:3}", Set.of());
    }

    @Test
    @DisplayName("패턴 검증 — 도메인 등록 attribute 토큰은 통과")
    void validate_attribute_token() {
        compiler.validate("{TYPE}-{SEQ:3}", Set.of("TYPE"));
    }

    @Test
    @DisplayName("패턴 검증 — 알 수 없는 토큰은 INVALID_PATTERN")
    void validate_fail_unknown_token() {
        assertThatThrownBy(() -> compiler.validate("{UNKNOWN}-{SEQ:3}", Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — {SEQ} (no-arg) 는 거부")
    void validate_fail_seq_no_arg() {
        assertThatThrownBy(() -> compiler.validate("{SEQ}", Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — SEQ 토큰이 없으면 INVALID_PATTERN")
    void validate_fail_no_seq_token() {
        assertThatThrownBy(() -> compiler.validate("D-{YYYY}", Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — SEQ 자릿수가 1~18 범위 밖이면 INVALID_PATTERN")
    void validate_fail_seq_length_out_of_range() {
        assertThatThrownBy(() -> compiler.validate("{SEQ:0}", Set.of()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> compiler.validate("{SEQ:19}", Set.of()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("렌더 — literal + SEQ:n 단순 패턴")
    void render_literal_and_seq() {
        String code = compiler.render("D{SEQ:3}", new RenderContext(4L, null, FIXED_DATE, Map.of()));
        assertThat(code).isEqualTo("D004");
    }

    @Test
    @DisplayName("렌더 — 연도/월/일 토큰 치환")
    void render_date_tokens() {
        assertThat(compiler.render("{YYYY}{SEQ:3}", new RenderContext(1L, null, FIXED_DATE, Map.of())))
                .isEqualTo("2026001");
        assertThat(compiler.render("{YY}{MM}{DD}{SEQ:3}",
                new RenderContext(1L, null, FIXED_DATE, Map.of())))
                .isEqualTo("260426001");
    }

    @Test
    @DisplayName("렌더 — PARENT 토큰은 컨텍스트 parentCode 로 치환")
    void render_parent_token() {
        String code = compiler.render("{PARENT}-{SEQ:3}",
                new RenderContext(2L, "D001", FIXED_DATE, Map.of()));
        assertThat(code).isEqualTo("D001-002");
    }

    @Test
    @DisplayName("렌더 — PARENT 토큰 사용 시 parentCode 미컨텍스트면 MISSING_PARENT_CONTEXT")
    void render_fail_missing_parent() {
        assertThatThrownBy(() ->
                compiler.render("{PARENT}-{SEQ:3}", new RenderContext(1L, null, FIXED_DATE, Map.of())))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.MISSING_PARENT_CONTEXT);
    }

    @Test
    @DisplayName("렌더 — attribute 토큰은 컨텍스트 매핑값으로 치환")
    void render_attribute_token() {
        String code = compiler.render("{TYPE}-{SEQ:3}",
                new RenderContext(1L, null, FIXED_DATE, Map.of("TYPE", "G")));
        assertThat(code).isEqualTo("G-001");
    }

    @Test
    @DisplayName("렌더 — attribute 매핑 누락 시 MISSING_ATTRIBUTE_MAPPING")
    void render_attribute_missing() {
        assertThatThrownBy(() -> compiler.render("{TYPE}-{SEQ:3}",
                new RenderContext(1L, null, FIXED_DATE, Map.of())))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.MISSING_ATTRIBUTE_MAPPING);
    }

    @Test
    @DisplayName("매칭 — 패턴에 부합하는 코드는 true")
    void matches_valid() {
        assertThat(compiler.matches("D{SEQ:3}", "D004")).isTrue();
        assertThat(compiler.matches("D-{YYYY}-{SEQ:4}", "D-2026-0042")).isTrue();
    }

    @Test
    @DisplayName("매칭 — literal 이 다른 코드는 false")
    void matches_different_literal() {
        assertThat(compiler.matches("D{SEQ:3}", "X004")).isFalse();
    }

    @Test
    @DisplayName("매칭 — SEQ 자릿수가 다른 코드는 false")
    void matches_wrong_seq_length() {
        assertThat(compiler.matches("D{SEQ:3}", "D04")).isFalse();
        assertThat(compiler.matches("D{SEQ:3}", "D0004")).isFalse();
    }

    @Test
    @DisplayName("PARENT 토큰 사용 여부 판별")
    void uses_parent_token() {
        assertThat(compiler.usesParentToken("{PARENT}-{SEQ:3}")).isTrue();
        assertThat(compiler.usesParentToken("D{SEQ:3}")).isFalse();
    }

    @Test
    @DisplayName("사용된 attribute key 추출")
    void used_attribute_keys() {
        assertThat(compiler.usedAttributeKeys("{TYPE}-{SEQ:3}", Set.of("TYPE", "STATUS")))
                .containsExactly("TYPE");
        assertThat(compiler.usedAttributeKeys("{TYPE}-{STATUS}-{SEQ:3}", Set.of("TYPE", "STATUS")))
                .containsExactly("TYPE", "STATUS");
        assertThat(compiler.usedAttributeKeys("D{SEQ:3}", Set.of("TYPE")))
                .isEmpty();
    }

    @Test
    @DisplayName("resetPolicy 자동 추론 — 날짜 토큰 조합")
    void resolve_reset_policy() {
        assertThat(compiler.resolveResetPolicy("D{SEQ:3}")).isEqualTo(ResetPolicy.NEVER);
        assertThat(compiler.resolveResetPolicy("{YYYY}{SEQ:3}")).isEqualTo(ResetPolicy.YEARLY);
        assertThat(compiler.resolveResetPolicy("{YY}{SEQ:3}")).isEqualTo(ResetPolicy.YEARLY);
        assertThat(compiler.resolveResetPolicy("{YYYY}{MM}{SEQ:3}")).isEqualTo(ResetPolicy.MONTHLY);
        assertThat(compiler.resolveResetPolicy("{YY}{MM}{DD}{SEQ:3}")).isEqualTo(ResetPolicy.DAILY);
    }
}
