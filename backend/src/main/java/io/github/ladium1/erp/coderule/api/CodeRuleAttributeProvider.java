package io.github.ladium1.erp.coderule.api;

import io.github.ladium1.erp.coderule.api.dto.CodeRuleAttributeDescriptor;

/**
 * 도메인이 자신의 분류 attribute 후보를 coderule 모듈에 등록하기 위한 인터페이스.
 * <p>
 * 도메인 모듈에서 이 인터페이스를 {@link org.springframework.stereotype.Component} 빈으로 구현하면
 * 채번 화면이 해당 도메인의 분류값 토큰 옵션과 가능 값 목록을 자동으로 노출한다.
 * <p>
 * 매핑 (sourceValue -> codeValue) 자체는 사용자가 채번 규칙 화면에서 정의한다.
 */
public interface CodeRuleAttributeProvider {

    CodeRuleTarget target();

    CodeRuleAttributeDescriptor descriptor();
}
