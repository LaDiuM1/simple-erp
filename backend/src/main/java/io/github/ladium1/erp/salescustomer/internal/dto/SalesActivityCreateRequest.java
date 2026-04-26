package io.github.ladium1.erp.salescustomer.internal.dto;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SalesActivityCreateRequest(
        @NotNull
        Long customerId,

        @NotNull
        SalesActivityType type,

        @NotNull
        LocalDateTime activityDate,

        @NotBlank @Size(max = 200)
        String subject,

        String content,

        @NotNull
        Long ourEmployeeId,

        /** 영업 명부 식별자 — null 이면 customerContactName / Position 자유 입력 fallback. */
        Long customerContactId,

        @Size(max = 100)
        String customerContactName,

        @Size(max = 100)
        String customerContactPosition
) {
}
