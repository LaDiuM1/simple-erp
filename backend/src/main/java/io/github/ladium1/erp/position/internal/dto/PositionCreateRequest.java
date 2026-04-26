package io.github.ladium1.erp.position.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PositionCreateRequest(
        /**
         * 직책 코드 — 채번 규칙의 inputMode 가 AUTO 면 무시되고 시스템이 생성한다.
         * MANUAL / AUTO_OR_MANUAL+직접입력 시 사용자 입력값을 패턴 검증 후 사용.
         */
        @Size(max = 50)
        String code,

        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
