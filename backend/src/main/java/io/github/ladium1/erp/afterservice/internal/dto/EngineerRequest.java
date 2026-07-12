package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.EngineerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 엔지니어 등록 / 수정 공용 요청 — 두 케이스의 입력 항목이 동일.
 */
public record EngineerRequest(
        @NotBlank @Size(max = 50)
        String name,

        @NotNull
        EngineerType type,

        @Size(max = 100)
        String affiliation,

        @Size(max = 30)
        String phone,

        /** 내부 구분일 때만 선택 입력 — 직원 링크 */
        Long employeeId,

        @NotNull
        Boolean active
) {
}
