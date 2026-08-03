package io.github.ladium1.erp.coderule.api.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * 채번 호출 시 도메인이 전달하는 컨텍스트.
 * <p>
 * {@code parentCode} 는 {@code {PARENT}} 토큰 또는 부모-범위 시퀀스 분리 시 사용된다.
 * {@code attributes} 는 도메인이 등록한 attribute key -> sourceValue 매핑.
 * 예: {@code Map.of("TYPE", "GENERAL")} 은 패턴의 {@code {TYPE}} 토큰을 GENERAL 매핑값으로 치환한다.
 * {@code generationDate} 는 계약일처럼 업무 기준일로 날짜 토큰과 시퀀스 scope를 결정할 때 사용한다.
 * 생략하면 호출 시점의 오늘을 사용한다.
 */
public record CodeGenerationContext(
        String parentCode,
        Map<String, String> attributes,
        LocalDate generationDate
) {

    public CodeGenerationContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static CodeGenerationContext empty() {
        return new CodeGenerationContext(null, Map.of(), null);
    }

    public static CodeGenerationContext ofParent(String parentCode) {
        return new CodeGenerationContext(parentCode, Map.of(), null);
    }

    public static CodeGenerationContext withAttributes(Map<String, String> attributes) {
        return new CodeGenerationContext(null, attributes, null);
    }

    public static CodeGenerationContext of(String parentCode, Map<String, String> attributes) {
        return new CodeGenerationContext(parentCode, attributes, null);
    }

    public static CodeGenerationContext onDate(LocalDate generationDate) {
        return new CodeGenerationContext(null, Map.of(), generationDate);
    }
}
