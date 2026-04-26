package io.github.ladium1.erp.coderule.api;

import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;

public interface CodeRuleApi {

    /**
     * 대상의 채번 규칙 조회.
     */
    CodeRuleInfo getRule(CodeRuleTarget target);

    /**
     * 다음 코드 생성 + 시퀀스 증가 (atomic).
     * <p>
     * 호출 트랜잭션에 참여하므로 도메인 저장 실패 시 시퀀스도 함께 롤백된다.
     */
    String generate(CodeRuleTarget target, CodeGenerationContext context);

    /**
     * 다음 생성될 코드 미리보기 — 시퀀스를 증가시키지 않는다.
     * <p>
     * UI 미리보기 / 등록 폼의 자동 모드 표시 용.
     */
    String preview(CodeRuleTarget target, CodeGenerationContext context);

    /**
     * 사용자가 직접 입력한 코드가 패턴에 부합하는지 검증.
     * <p>
     * 부합하지 않으면 BusinessException 을 throw 한다. 유일성 검증은 도메인 책임.
     */
    void validate(CodeRuleTarget target, String code);
}
