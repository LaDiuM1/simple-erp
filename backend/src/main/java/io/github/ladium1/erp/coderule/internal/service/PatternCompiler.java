package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 패턴 토큰 컴파일러.
 *
 * <pre>
 *   literal 문자열      그대로 코드에 박힘 (예: "EMP-")
 *   {YYYY} {YY}        연도 4 / 2 자리
 *   {MM} {DD}          월 / 일 2 자리
 *   {SEQ:n}            시퀀스 (n 자리 zero-pad). n 은 필수.
 *   {PARENT}           컨텍스트의 parentCode
 *   {도메인-등록 KEY}  도메인이 CodeRuleAttributeProvider 로 등록한 분류 attribute (예: {TYPE})
 * </pre>
 */
@Component
public class PatternCompiler {

    private static final Pattern TOKEN = Pattern.compile("\\{([A-Z]+)(?::(\\d+))?}");
    private static final Set<String> BUILT_IN_TOKENS = Set.of(
            "YYYY", "YY", "MM", "DD", "SEQ", "PARENT");
    private static final int MAX_SEQ_LENGTH = 18;

    public record RenderContext(
            long seq,
            String parentCode,
            LocalDate now,
            Map<String, String> attributes
    ) {
    }

    public void validate(String pattern, Set<String> attributeKeys) {
        if (pattern == null || pattern.isBlank()) {
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }
        Matcher m = TOKEN.matcher(pattern);
        boolean hasSeq = false;
        while (m.find()) {
            String name = m.group(1);
            String arg = m.group(2);

            boolean isBuiltIn = BUILT_IN_TOKENS.contains(name);
            boolean isAttribute = attributeKeys != null && attributeKeys.contains(name);
            if (!isBuiltIn && !isAttribute) {
                throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
            }
            if (arg != null && !"SEQ".equals(name)) {
                throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
            }
            if ("SEQ".equals(name)) {
                hasSeq = true;
                if (arg == null) {
                    // {SEQ} (no-arg) 폐기 -> 자릿수 명시 필수
                    throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
                }
                int n = Integer.parseInt(arg);
                if (n < 1 || n > MAX_SEQ_LENGTH) {
                    throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
                }
            }
        }
        if (!hasSeq) {
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }
    }

    public String render(String pattern, RenderContext ctx) {
        StringBuilder result = new StringBuilder();
        Matcher m = TOKEN.matcher(pattern);
        int last = 0;
        while (m.find()) {
            result.append(pattern, last, m.start());
            result.append(resolveToken(m.group(1), m.group(2), ctx));
            last = m.end();
        }
        result.append(pattern, last, pattern.length());
        return result.toString();
    }

    public boolean matches(String pattern, String code) {
        if (code == null) {
            return false;
        }
        return toRegex(pattern).matcher(code).matches();
    }

    public boolean usesParentToken(String pattern) {
        Matcher m = TOKEN.matcher(pattern);
        while (m.find()) {
            if ("PARENT".equals(m.group(1))) {
                return true;
            }
        }
        return false;
    }

    public Set<String> usedAttributeKeys(String pattern, Set<String> attributeKeys) {
        if (attributeKeys == null || attributeKeys.isEmpty()) {
            return Set.of();
        }
        Set<String> used = new LinkedHashSet<>();
        Matcher m = TOKEN.matcher(pattern);
        while (m.find()) {
            String name = m.group(1);
            if (attributeKeys.contains(name)) {
                used.add(name);
            }
        }
        return used;
    }

    /**
     * 패턴의 날짜 토큰 조합으로 시퀀스 초기화 주기를 자동 추론.
     * <pre>
     *   {DD}  -> DAILY
     *   {MM}  -> MONTHLY
     *   {YYYY} 또는 {YY}  -> YEARLY
     *   날짜 토큰 없음   -> NEVER
     * </pre>
     */
    public ResetPolicy resolveResetPolicy(String pattern) {
        boolean hasYear = false, hasMonth = false, hasDay = false;
        Matcher m = TOKEN.matcher(pattern);
        while (m.find()) {
            switch (m.group(1)) {
                case "YYYY", "YY" -> hasYear = true;
                case "MM" -> hasMonth = true;
                case "DD" -> hasDay = true;
                default -> { /* skip */ }
            }
        }
        if (hasDay) return ResetPolicy.DAILY;
        if (hasMonth) return ResetPolicy.MONTHLY;
        if (hasYear) return ResetPolicy.YEARLY;
        return ResetPolicy.NEVER;
    }

    private String resolveToken(String name, String arg, RenderContext ctx) {
        return switch (name) {
            case "YYYY"   -> String.format("%04d", ctx.now().getYear());
            case "YY"     -> String.format("%02d", ctx.now().getYear() % 100);
            case "MM"     -> String.format("%02d", ctx.now().getMonthValue());
            case "DD"     -> String.format("%02d", ctx.now().getDayOfMonth());
            case "SEQ"    -> formatSeq(ctx.seq(), arg);
            case "PARENT" -> {
                if (ctx.parentCode() == null) {
                    throw new BusinessException(CodeRuleErrorCode.MISSING_PARENT_CONTEXT);
                }
                yield ctx.parentCode();
            }
            default -> {
                String mapped = ctx.attributes() == null ? null : ctx.attributes().get(name);
                if (mapped == null) {
                    throw new BusinessException(CodeRuleErrorCode.MISSING_ATTRIBUTE_MAPPING);
                }
                yield mapped;
            }
        };
    }

    private String formatSeq(long seq, String arg) {
        if (arg == null) {
            // validate 가 막아야 하지만 안전상
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }
        int width = Integer.parseInt(arg);
        return String.format("%0" + width + "d", seq);
    }

    private Pattern toRegex(String pattern) {
        StringBuilder sb = new StringBuilder("^");
        Matcher m = TOKEN.matcher(pattern);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                sb.append(Pattern.quote(pattern.substring(last, m.start())));
            }
            sb.append(tokenToRegex(m.group(1), m.group(2)));
            last = m.end();
        }
        if (last < pattern.length()) {
            sb.append(Pattern.quote(pattern.substring(last)));
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    private String tokenToRegex(String name, String arg) {
        return switch (name) {
            case "YYYY"   -> "\\d{4}";
            case "YY", "MM", "DD" -> "\\d{2}";
            case "SEQ"    -> arg != null ? "\\d{" + arg + "}" : "\\d+";
            // PARENT / 도메인 attribute 토큰 — 외부 결정 사항이라 느슨하게 매칭
            default -> ".+?";
        };
    }
}
