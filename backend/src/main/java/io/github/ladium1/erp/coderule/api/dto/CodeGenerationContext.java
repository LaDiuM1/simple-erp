package io.github.ladium1.erp.coderule.api.dto;

import java.util.Map;

/**
 * 채번 호출 시 도메인이 전달하는 컨텍스트.
 * <p>
 * {@code parentCode} 는 {@code {PARENT}} 토큰 또는 부모-범위 시퀀스 분리 시 사용된다.
 * {@code attributes} 는 도메인이 등록한 attribute key -> sourceValue 매핑.
 * 예: {@code Map.of("TYPE", "GENERAL")} 은 패턴의 {@code {TYPE}} 토큰을 GENERAL 매핑값으로 치환한다.
 */
public record CodeGenerationContext(String parentCode, Map<String, String> attributes) {

    public CodeGenerationContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static CodeGenerationContext empty() {
        return new CodeGenerationContext(null, Map.of());
    }

    public static CodeGenerationContext ofParent(String parentCode) {
        return new CodeGenerationContext(parentCode, Map.of());
    }

    public static CodeGenerationContext withAttributes(Map<String, String> attributes) {
        return new CodeGenerationContext(null, attributes);
    }

    public static CodeGenerationContext of(String parentCode, Map<String, String> attributes) {
        return new CodeGenerationContext(parentCode, attributes);
    }
}
