package io.github.ladium1.erp.coderule.api.dto;

import lombok.Builder;

import java.util.List;

/**
 * 도메인이 채번 모듈에 등록하는 attribute 후보 메타.
 * <p>
 * {@code key} 는 곧 패턴 토큰명 (대문자, 예: TYPE -> {TYPE}).
 * {@code values} 는 도메인의 분류 enum / 후보 — 사용자는 채번 규칙 화면에서 각 source 에
 * 매핑할 codeValue (코드 안에 들어갈 prefix 문자열) 을 정의한다.
 */
@Builder
public record CodeRuleAttributeDescriptor(
        String key,
        String label,
        List<AttributeValue> values
) {

    @Builder
    public record AttributeValue(String value, String label) {
    }
}
