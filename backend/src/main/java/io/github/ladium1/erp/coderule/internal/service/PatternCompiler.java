package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 패턴 토큰 컴파일러.
 *
 * <pre>
 *   {PREFIX}              규칙의 prefix 로 치환
 *   {YYYY} {YY}           연도 4 / 2 자리
 *   {MM} {DD}             월 / 일 2 자리
 *   {SEQ}                 시퀀스 (defaultSeqLength 로 zero-pad)
 *   {SEQ:n}               시퀀스 (n 자리 zero-pad)
 *   {PARENT}              컨텍스트의 parentCode
 * </pre>
 *
 * 그 외 문자는 literal 로 취급. 알 수 없는 토큰은 INVALID_PATTERN.
 */
@Component
public class PatternCompiler {

    private static final Pattern TOKEN = Pattern.compile("\\{([A-Z]+)(?::(\\d+))?}");
    private static final Set<String> KNOWN_TOKENS = Set.of("PREFIX", "YYYY", "YY", "MM", "DD", "SEQ", "PARENT");
    private static final int MAX_SEQ_LENGTH = 18;

    public record RenderContext(
            String prefix,
            long seq,
            int defaultSeqLength,
            String parentCode,
            LocalDate now
    ) {
    }

    /**
     * 패턴 자체의 유효성 검증. 알 수 없는 토큰 / SEQ 자릿수 범위 / SEQ 부재 등을 검사한다.
     */
    public void validate(String pattern, int defaultSeqLength) {
        if (pattern == null || pattern.isBlank()) {
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }
        if (defaultSeqLength < 1 || defaultSeqLength > MAX_SEQ_LENGTH) {
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }

        Matcher m = TOKEN.matcher(pattern);
        boolean hasSeq = false;
        while (m.find()) {
            String name = m.group(1);
            String arg = m.group(2);

            if (!KNOWN_TOKENS.contains(name)) {
                throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
            }
            if (arg != null && !"SEQ".equals(name)) {
                throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
            }
            if ("SEQ".equals(name)) {
                hasSeq = true;
                if (arg != null) {
                    int n = Integer.parseInt(arg);
                    if (n < 1 || n > MAX_SEQ_LENGTH) {
                        throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
                    }
                }
            }
        }
        if (!hasSeq) {
            // SEQ 없이는 같은 시점에 반복 코드가 발생 -> 유일성 보장 불가
            throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        }
    }

    /**
     * 패턴에 컨텍스트를 적용해 최종 코드 문자열 생성.
     */
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

    /**
     * 사용자가 입력한 코드가 패턴에 부합하는지 매칭.
     */
    public boolean matches(String pattern, String prefix, String code) {
        if (code == null) {
            return false;
        }
        return toRegex(pattern, prefix).matcher(code).matches();
    }

    /**
     * 패턴 안에 {PARENT} 토큰이 포함되는지.
     */
    public boolean usesParentToken(String pattern) {
        Matcher m = TOKEN.matcher(pattern);
        while (m.find()) {
            if ("PARENT".equals(m.group(1))) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(String name, String arg, RenderContext ctx) {
        return switch (name) {
            case "PREFIX" -> ctx.prefix() == null ? "" : ctx.prefix();
            case "YYYY"   -> String.format("%04d", ctx.now().getYear());
            case "YY"     -> String.format("%02d", ctx.now().getYear() % 100);
            case "MM"     -> String.format("%02d", ctx.now().getMonthValue());
            case "DD"     -> String.format("%02d", ctx.now().getDayOfMonth());
            case "SEQ"    -> formatSeq(ctx.seq(), arg, ctx.defaultSeqLength());
            case "PARENT" -> {
                if (ctx.parentCode() == null) {
                    throw new BusinessException(CodeRuleErrorCode.MISSING_PARENT_CONTEXT);
                }
                yield ctx.parentCode();
            }
            default -> throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        };
    }

    private String formatSeq(long seq, String arg, int defaultLength) {
        int width = arg != null ? Integer.parseInt(arg) : defaultLength;
        return String.format("%0" + width + "d", seq);
    }

    private Pattern toRegex(String pattern, String prefix) {
        StringBuilder sb = new StringBuilder("^");
        Matcher m = TOKEN.matcher(pattern);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                sb.append(Pattern.quote(pattern.substring(last, m.start())));
            }
            sb.append(tokenToRegex(m.group(1), m.group(2), prefix));
            last = m.end();
        }
        if (last < pattern.length()) {
            sb.append(Pattern.quote(pattern.substring(last)));
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    private String tokenToRegex(String name, String arg, String prefix) {
        return switch (name) {
            case "PREFIX" -> Pattern.quote(prefix == null ? "" : prefix);
            case "YYYY"   -> "\\d{4}";
            case "YY", "MM", "DD" -> "\\d{2}";
            case "SEQ"    -> arg != null ? "\\d{" + arg + "}" : "\\d+";
            // 부모 코드 형식은 모듈 외부 결정 사항이라 느슨하게 매칭
            case "PARENT" -> ".+?";
            default -> throw new BusinessException(CodeRuleErrorCode.INVALID_PATTERN);
        };
    }
}
