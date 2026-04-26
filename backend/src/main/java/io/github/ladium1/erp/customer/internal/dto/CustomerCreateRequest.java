package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerCreateRequest(
        /**
         * 고객사 코드 — 채번 규칙의 inputMode 가 AUTO 면 무시되고 시스템이 생성한다.
         * MANUAL / AUTO_OR_MANUAL+직접입력 시 사용자 입력값을 패턴 검증 후 사용.
         */
        @Size(max = 50)
        String code,

        @NotBlank @Size(max = 200)
        String name,

        @Size(max = 200)
        String nameEn,

        @Size(max = 20)
        String bizRegNo,

        @Size(max = 20)
        String corpRegNo,

        @Size(max = 50)
        String representative,

        @Size(max = 100)
        String bizType,

        @Size(max = 100)
        String bizItem,

        @Size(max = 30)
        String phone,

        @Size(max = 30)
        String fax,

        @Email @Size(max = 100)
        String email,

        @Size(max = 200)
        String website,

        @Size(max = 10)
        String zipCode,

        @Size(max = 200)
        String roadAddress,

        @Size(max = 200)
        String detailAddress,

        @NotNull
        CustomerType type,

        @NotNull
        CustomerStatus status,

        LocalDate tradeStartDate,

        String note
) {
}
