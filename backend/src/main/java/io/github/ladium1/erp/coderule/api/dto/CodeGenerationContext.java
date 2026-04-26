package io.github.ladium1.erp.coderule.api.dto;

/**
 * 채번 호출 시 도메인이 전달하는 컨텍스트.
 * <p>
 * {@code parentCode} 는 {@code {PARENT}} 토큰 또는 {@code parentScoped} 시퀀스 분리 시 사용된다.
 * 토큰이 없거나 분리가 꺼져 있으면 무시된다.
 */
public record CodeGenerationContext(String parentCode) {

    public static CodeGenerationContext empty() {
        return new CodeGenerationContext(null);
    }

    public static CodeGenerationContext ofParent(String parentCode) {
        return new CodeGenerationContext(parentCode);
    }
}
